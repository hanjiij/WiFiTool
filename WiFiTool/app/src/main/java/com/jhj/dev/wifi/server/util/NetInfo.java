package com.jhj.dev.wifi.server.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: IPv6 support

/**
 * @author 吉鹏
 */
public class NetInfo {
    public static final String NOIP = "0.0.0.0";
    public static final String NOMASK = "255.255.255.255";
    public static final String NOMAC = "00:00:00:00:00:00";
    public static final String KEY_IP_CUSTOM = "ip_custom";
    public static final boolean DEFAULT_IP_CUSTOM = false;
    public static final String KEY_IP_START = "ip_start";//开始
    public static final String DEFAULT_IP_START = "0.0.0.0";
    public static final String KEY_IP_END = "ip_end";//结束
    public static final String DEFAULT_IP_END = "0.0.0.0";
    public static final String KEY_CIDR_CUSTOM = "cidr_custom";
    public static final boolean DEFAULT_CIDR_CUSTOM = false;
    public static final String KEY_CIDR = "cidr";
    public static final String DEFAULT_CIDR = "24";
    public static final String KEY_INTF = "interface";
    public static final String DEFAULT_INTF = null;
    private static final int BUF = 8 * 1024;
    private static final String CMD_IP = " -f inet addr show %s";
    private static final String PTN_IP1 =
            "\\s*inet [0-9\\.]+\\/([0-9]+) brd [0-9\\.]+ scope global %s$";
    private static final String NOIF = "0";
    public static String macAddress = NOMAC;//"00:00:00:00:00:00"
    private final String TAG = "NetInfo";
    public String intf = "eth0";
    public String ip = NOIP;
    public int cidr = 24;
    public int speed = 0;
    public String ssid = null;
    public String bssid = null;
    public String carrier = null;
    public String netmaskIp = NOMASK;//"255.255.255.255";
    public String gatewayIp = NOIP;//"0.0.0.0"
    private Context ctxt;
    private WifiInfo info;
    private SharedPreferences prefs;


    public NetInfo(final Context ctxt) {
        this.ctxt = ctxt;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        getIp();
        getWifiInfo();
    }

    //判断是否连接到网络
    public static boolean isConnected(Context ctxt) {
        NetworkInfo nfo =
                ((ConnectivityManager) ctxt.getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo();
        if (nfo != null) {
            return nfo.isConnected();
        }
        //        Toast.makeText(ctxt, "qisdcsvzfxd", Toast.LENGTH_SHORT).show();
        return false;
    }

    //获得ip变成一串数字
    public static long getUnsignedLongFromIp(String ip_addr) {
        String[] a = ip_addr.split("\\.");
        Log.i("aa", "======>" +
                    (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536 +
                     Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3])) + "");
        return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536 +
                Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3]));
    }

    //获得网关
    public static String getIpFromIntSigned(int ip_int) {
        String ip = "";
        for (int k = 0; k < 4; k++) {
            ip = ip + ((ip_int >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    //获得前三位的子串
    public static String getIpFromLongUnsigned(long ip_long) {
        String ip = "";
        for (int k = 3; k > -1; k--) {
            ip = ip + ((ip_long >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    public void getIp() {
        intf = prefs.getString(KEY_INTF, DEFAULT_INTF);
        try {
            if (intf == DEFAULT_INTF || NOIF.equals(intf)) {
                // 自动选择接口
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                     en.hasMoreElements(); ) {
                    //列举集合。枚举
                    NetworkInterface ni = en.nextElement();
                    intf = ni.getName();//枚举获取信号名，如无线网（eth0）等
                    Log.i("intf", "======>" + intf);
                    ip = getInterfaceFirstIp(ni);
                    if (ip != NOIP) {
                        break;
                    }
                }
            } else {
                // 定义接口参数
                ip = getInterfaceFirstIp(NetworkInterface.getByName(intf));
            }
        } catch (SocketException e) {
            Log.e(TAG, e.getMessage());
        }
        getCidr();//跑ip的方法
    }

    //把ip清除归零
    private String getInterfaceFirstIp(NetworkInterface ni) {
        if (ni != null) {
            for (Enumeration<InetAddress> nis = ni.getInetAddresses(); nis.hasMoreElements(); ) {
                InetAddress ia = nis.nextElement();
                if (!ia.isLoopbackAddress()) {
                    if (ia instanceof Inet6Address) {
                        Log.i(TAG, "IPv6 detected and not supported yet!");
                        continue;
                    }
                    return ia.getHostAddress();
                }
            }
        }
        return NOIP;
    }

    private void getCidr() {
        if (netmaskIp != NOMASK) {//子网掩码255.255.255.255
            cidr = IpToCidr(netmaskIp);
        } else {
            String match;
            //跑ip的工具
            try {
                if ((match = runCommand("/system/xbin/ip", String.format(CMD_IP, intf),
                                        String.format(PTN_IP1, intf))) != null)
                {
                    cidr = Integer.parseInt(match);
                    return;
                }
            } catch (NumberFormatException e) {
                Log.i(TAG, e.getMessage() + " -> cannot find cidr, using default /24");
            }
        }
    }

    // FIXME: Factorize, this isn't a generic runCommand()
    private String runCommand(String path, String cmd, String ptn) {
        try {
            if (new File(path).exists() == true) {
                String line;
                Matcher matcher;
                Pattern ptrn = Pattern.compile(ptn);//读取文件路径
                Process p = Runtime.getRuntime().exec(path + cmd);//拿到字串
                BufferedReader r =
                        new BufferedReader(new InputStreamReader(p.getInputStream()), BUF);
                while ((line = r.readLine()) != null) {
                    matcher = ptrn.matcher(line);//读取到了一行数据
                    if (matcher.matches()) {
                        System.out.println("runCommand--------------->" + matcher.group(1));
                        return matcher.group(1);//返回匹配到的数据
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't use native command: " + e.getMessage());
            return null;
        }
        return null;
    }

    //获取本地ip和mac地址
    public boolean getWifiInfo() {
        WifiManager wifi = (WifiManager) ctxt.getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            info = wifi.getConnectionInfo();
            // Set com.ccit.dev.wifis variables
            speed = info.getLinkSpeed();
            ssid = info.getSSID();
            bssid = info.getBSSID();
            macAddress = info.getMacAddress();
            System.out.println("macAddress=============>" + macAddress);
            gatewayIp = getIpFromIntSigned(wifi.getDhcpInfo().gateway);//ip的首选地址，确定局域网的范围

            netmaskIp = getIpFromIntSigned(wifi.getDhcpInfo().netmask);
            return true;
        }
        //        Toast.makeText(ctxt, "qisdcsvzfxd", Toast.LENGTH_SHORT).show();
        return false;
    }

    //返回ip
    public String getNetIp() {
        int shift = (32 - cidr);
        int start = ((int) getUnsignedLongFromIp(ip) >> shift << shift);
        System.out.println("iiiiiiiiii------>" + start);
        return getIpFromLongUnsigned((long) start);
    }

    //返回WiFi是连接的
    public SupplicantState getSupplicantState() {
        return info.getSupplicantState();
    }

    //通过ip拿子网掩码0
    private int IpToCidr(String ip) {
        double sum = -2;
        String[] part = ip.split("\\.");
        for (String p : part) {
            sum += 256D - Double.parseDouble(p);
        }
        Log.i("IpToCidr", "---------->" + (32 - (int) (Math.log(sum) / Math.log(2d))));
        return 32 - (int) (Math.log(sum) / Math.log(2d));

    }

}
