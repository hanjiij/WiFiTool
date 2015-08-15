package com.jhj.dev.wifi.server.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jhj.dev.wifi.server.DrawerActivity;
import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.mydatabase.MyDatabase;
import com.jhj.dev.wifi.server.util.AbstractDiscovery;
import com.jhj.dev.wifi.server.util.DefaultDiscovery;
import com.jhj.dev.wifi.server.util.HostBean;
import com.jhj.dev.wifi.server.util.NetInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 吉鹏
 */
public class FinderFragment extends Fragment {

    public static HostsAdapter adapter;
    public static SQLiteDatabase db;
    private static LayoutInflater mInflater;
    public SharedPreferences prefs = null;
    public NetInfo net = null;
    protected String info_ip_str = "";
    protected String info_in_str = "";
    protected String info_mo_str = "";
    protected String info_mac_str = "";
    Button scanButton, drawButton;
    ListView showListView;
    TextView ipTextView, ssidTextView, modeTextView, mac;
    AbstractDiscovery mDiscoveryTask = null;// 专门进行异步数据加载
    private long network_ip = 0;
    private long network_start = 0;
    private long network_end = 0;
    private List<HostBean> hosts = null;// 资源可序列化集合
    private ConnectivityManager connMgr;
    //	OnDateChangedListener onDateChangedListener;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            // WiFi 状态
            String action = intent.getAction();// 获得广播类型
            if (action != null) {
                if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    int WifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    // Log.d(TAG, "WifiState=" + WifiState);
                    switch (WifiState) {
                        case WifiManager.WIFI_STATE_ENABLING:
                            info_in_str = "Wifi is enabling";
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            info_in_str = "Wifi is enabled";
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            info_in_str = "Wifi is enabled";
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            info_in_str = "Wifi is disabled";
                            break;
                        default:
                            info_in_str = "Wifi state unknown";
                    }
                }

                if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) &&
                    net.getWifiInfo())
                {
                    SupplicantState sstate = net.getSupplicantState();
                    // Log.d(TAG, "SupplicantState=" + sstate);
                    if (sstate == SupplicantState.SCANNING) {// 是否正在扫描
                        info_in_str = "Wifi is disconnected";
                    } else if (sstate == SupplicantState.ASSOCIATING) {
                        info_in_str = getString(R.string.wifi_associating,
                                                (net.ssid != null ? net.ssid
                                                                  : (net.bssid != null ? net.bssid
                                                                                       : net.macAddress)));
                    } else if (sstate == SupplicantState.COMPLETED) {
                        info_in_str = getString(R.string.wifi_dhcp, net.ssid);
                    }
                }
            }

            // 3G(connected) -> Wifi(connected)
            // Support Ethernet, with ConnectivityManager.TYPE_ETHER=3
            final NetworkInfo ni = connMgr.getActiveNetworkInfo();

            if (ni != null) {
                // Log.i(TAG, "NetworkState="+ni.getDetailedState());
                if (ni.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    int type = ni.getType();
                    // Log.i(TAG, "NetworkType="+type);
                    if (type == ConnectivityManager.TYPE_WIFI) { // WIFI
                        net.getWifiInfo();
                        if (net.ssid != null) {
                            net.getIp();
                            info_ip_str = getString(R.string.net_ip, net.ip, net.cidr, net.intf);
                            info_in_str = getString(R.string.net_ssid, net.ssid);
                            info_mo_str = getString(R.string.net_mode,
                                                    getString(R.string.net_mode_wifi, net.speed,
                                                              WifiInfo.LINK_SPEED_UNITS));
                            info_mac_str = net.macAddress;
                        }
                    }
                    // Always update network info
                    setInfo();
                }
            } else {
                Toast.makeText(ctxt, "当前没有网络，请连接网络！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void startDiscovering() {
        // TODO Auto-generated method stub
        //		mDiscoveryTask = DefaultDiscovery.getInstance(FinderFragment.this);
        mDiscoveryTask = new DefaultDiscovery(FinderFragment.this);
        mDiscoveryTask.setNetwork(network_ip, network_start, network_end);// 初始化ip
        mDiscoveryTask.execute();// 执行

        makeToast("开始扫描....请稍候");
        getActivity().setProgressBarVisibility(true);// 显示进度条
        initList();
    }

    private void initList() {
        // TODO Auto-generated method stub
        adapter.clear();// 清零数据
        hosts = new ArrayList<HostBean>();// 实例化集合
    }

    public void stopDiscovering() {

        mDiscoveryTask = null;
        System.gc();
        makeToast("网上邻居已经发现完毕！");
        scanButton.setEnabled(true);
        getActivity().setProgressBarVisibility(false);

    }

    public void addHost(HostBean host) {
        host.position = hosts.size();
        Log.i("pos", "host----------------->" + host);
        hosts.add(host);// 增加列表项
        adapter.add(null);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        net = new NetInfo(getActivity().getApplicationContext());
        prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());
        connMgr = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);// 获得网络权限
        mInflater = LayoutInflater.from(getActivity().getApplicationContext());// listview界面列表加载

        db = new MyDatabase(getActivity(), "Points").getWritableDatabase();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.find_activity, null);
        init(view);
        inisetListener();
        adapter = new HostsAdapter(getActivity().getApplicationContext());
        showListView.setAdapter(adapter);

        return view;
    }

    private void inisetListener() {
        // TODO Auto-generated method stub
        scanButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                scanButton.setEnabled(false);
                startDiscovering();// 扫描
            }

        });

        drawButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), DrawerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setInfo() {
        // TODO Auto-generated method stub
        ipTextView.setText(info_ip_str);
        ssidTextView.setText(info_in_str);
        modeTextView.setText(info_mo_str);
        mac.setText("Mac:" + info_mac_str);

        // Get ip information
        network_ip = NetInfo.getUnsignedLongFromIp(net.ip);// NetInfo这里只是提供一些最初的true或者false
        if (prefs.getBoolean(NetInfo.KEY_IP_CUSTOM, NetInfo.DEFAULT_IP_CUSTOM)
            // false
                ) {// 初始为false
            // Custom IP
            network_start = NetInfo.getUnsignedLongFromIp(
                    prefs.getString(NetInfo.KEY_IP_START, NetInfo.DEFAULT_IP_START)

                                                         );// network_start为0.0.0.0
            network_end = NetInfo.getUnsignedLongFromIp(
                    prefs.getString(NetInfo.KEY_IP_END, NetInfo.DEFAULT_IP_END)

                                                       );// network_end0.0.0.0
        } else {
            // Custom CIDR
            if (prefs.getBoolean(NetInfo.KEY_CIDR_CUSTOM, NetInfo.DEFAULT_CIDR_CUSTOM)) // false
            {
                net.cidr = Integer.parseInt(
                        prefs.getString(NetInfo.KEY_CIDR, NetInfo.DEFAULT_CIDR));// 获取cidr为24
            }
            // Detected IP
            int shift = (32 - net.cidr);
            if (net.cidr < 31) {
                network_start = (network_ip >> shift << shift) + 1;
                network_end = (network_start | ((1 << shift) - 1)) - 1;
            } else {
                network_start = (network_ip >> shift << shift);
                network_end = (network_start | ((1 << shift) - 1));
            }
        }
    }

    private void init(View view) {
        // TODO Auto-generated method stub
        scanButton = (Button) view.findViewById(R.id.button_find);
        drawButton = (Button) view.findViewById(R.id.button_Draw);
        showListView = (ListView) view.findViewById(R.id.listView_show);
        ipTextView = (TextView) view.findViewById(R.id.info_ip);
        ssidTextView = (TextView) view.findViewById(R.id.info_ssid);
        modeTextView = (TextView) view.findViewById(R.id.info_mode);
        mac = (TextView) view.findViewById(R.id.info_mac);
    }

    public void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);// 接收网络连接广播
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);// 接收WiFi状态改变广播
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        getActivity().unregisterReceiver(receiver);// 取消广播
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.network_topology_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop()
    {
        if (mDiscoveryTask != null) {
            mDiscoveryTask.cancel(true);
        }

        getActivity().setProgressBarVisibility(false);
        super.onStop();
    }

    static class ViewHolder {
        TextView host;
        TextView mac;
        ImageView logo;
    }

    @SuppressLint("InflateParams")
    public class HostsAdapter extends ArrayAdapter<Void> {
        public HostsAdapter(Context ctxt) {
            super(ctxt, R.layout.wifi_info, R.id.list);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.wifi_info, null);// 加载view界面
                holder = new ViewHolder();// 第一次初始化之后，就可以重复使用
                holder.host = (TextView) convertView.findViewById(R.id.list);
                holder.mac = (TextView) convertView.findViewById(R.id.mac);
                holder.logo = (ImageView) convertView.findViewById(R.id.logo);
                convertView.setTag(holder);// 通过setTag方法往列表中添加view对象
            } else {
                holder = (ViewHolder) convertView.getTag();// 添加view
            }
            //
            final HostBean host = hosts.get(position);
            // 对设备类型的判断
            if (host.deviceType == HostBean.TYPE_GATEWAY) {
                holder.logo.setImageResource(R.drawable.myrouter);// 为网关，路由器
            } else if (host.isAlive == 1 || !host.hardwareAddress.equals(NetInfo.NOMAC)) {
                holder.logo.setImageResource(R.drawable.computer);// 局域网内的电脑
            }
            if (host.hostname != null && !host.hostname.equals(host.ipAddress)) {
                holder.host.setText(host.hostname + " (" + host.ipAddress + ")");// 获取主机名的这一步
            } else {
                holder.host.setText(host.ipAddress);

            }
            if (!host.hardwareAddress.equals(NetInfo.NOMAC)) {
                holder.mac.setText(host.hardwareAddress);// 添加mac
                Log.i("host", "______>" + host.hardwareAddress);
            }
            return convertView;
        }
    }


}
