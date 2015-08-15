package com.jhj.dev.wifi.server.util;

import android.app.Activity;
import android.database.Cursor;

import com.jhj.dev.wifi.server.fragment.FinderFragment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 吉鹏
 */
public class HardwareAddress {

    private final static String MAC_RE = "^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int BUF = 8 * 1024;
    static String macDates;
    static ArrayList<String> arrayList = new ArrayList<String>();
    // static String name;
    private OnNameListener onNameListener;

    public HardwareAddress(Activity activity) {

    }

    public static String getHardwareAddress(String ip) {

        String hw = NetInfo.NOMAC;
        BufferedReader bufferedReader = null;
        try {
            if (ip != null) {
                String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
                Pattern pattern = Pattern.compile(ptrn);// ip数据匹配
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        hw = matcher.group(1);
                        // System.out.println("++++++>" + hw);
                        // getxml("http://mac.51240.com"+"/"+hw+"__mac/");
                        // break;
                        HardwareAddress.macDates = hw.substring(0, 8).toUpperCase();
                        System.out.println("++++++>" + HardwareAddress.macDates);
                        Cursor cursor;
                        String name;
                        try {

                            // System.out.println("开始============================");
                            System.out.println("数据库1" + hw.substring(0, 8));

                            cursor = FinderFragment.db
                                    .rawQuery("select * from MacInfoSql  where mac=?",
                                              new String[]{HardwareAddress.macDates});
                            cursor.moveToNext();
                            if ((name = cursor.getString(1)) != null) {
                                if (name.length() > 10) {
                                    name = name.substring(0, 10);
                                }
                                arrayList.add(name);
                                break;
                            }

                        } catch (Exception e) {
                            // // TODO Auto-generated catch block
                            getxml("http://mac.51240.com" + "/" + HardwareAddress.macDates +
                                   "__mac/");
                            System.out.println("数据库没成功，正在联网查找");
                            break;
                        }
                    }
                }
            } else {
                // Log.e(TAG, "ip is null");
            }
        } catch (IOException e) {
            // Log.e(TAG, "Can't open/read file ARP: " + e.getMessage());
            return hw;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                // Log.e(TAG, e.getMessage());
            }
        }

        return hw;
    }

    // 联网查找
    public static void getxml(String urlstr) {

        try {
            URL url = new URL(urlstr);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            InputStreamReader inputStreamReader =
                    new InputStreamReader(httpURLConnection.getInputStream());

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = "";
            String name[] = null;
            while ((line = bufferedReader.readLine()) != null) {

                try {
                    if (line.equals("<td bgcolor=\"#F5F5F5\" align=\"center\">厂商</td>")) {

                        line = bufferedReader.readLine();
                        String names = line.split(">")[1].split("<")[0];
                        name = names.split(" ");
                        arrayList.add(name[0]);
                        System.out.println("===>>>>>" + name[0]);

                        // System.out.println("===>>>>>" + name[0]);
                    }
                    break;

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    name[0] = "未知设备";
                    arrayList.add(name[0]);
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // name[0]="未知设备";
            arrayList.add("未知设备");
            System.out.println("解析失败");

        }
    }

    public void setOnNameListener(OnNameListener onNameListener) {
        this.onNameListener = onNameListener;
        getDates();
    }

    private void getDates() {
        // TODO Auto-generated method stub
        // getxml("http://mac.51240.com" + "/" + NetInfo.macAddress + "__mac/");
        onNameListener.onNameArrayList(arrayList);
    }

    public interface OnNameListener {
        void onNameArrayList(ArrayList<String> arrayList);
    }
}
