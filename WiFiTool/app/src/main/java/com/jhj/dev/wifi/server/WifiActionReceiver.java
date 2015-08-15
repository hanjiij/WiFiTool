package com.jhj.dev.wifi.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.text.format.Formatter;

import com.jhj.dev.wifi.server.wifirssimap.WifiRSSIMonitor;

/**
 * @author 江华健
 */
public class WifiActionReceiver extends BroadcastReceiver {

    private static final String ACTION_WIFI_STATE_CHANGED = WifiManager.WIFI_STATE_CHANGED_ACTION;
    private static final String ACTION_NETWORK_STATE_CHANGED =
            WifiManager.NETWORK_STATE_CHANGED_ACTION;
    private static final String ACITON_SCAN_RESULTS_AVAILABLE =
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
    private static final String ACTION_SUPPLICANT_CONNECTION_CHANGE =
            WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION;
    private static final String ACTION_SUPPLICANT_STATE_CHANGED =
            WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
    private static final String ACTION_NETWORK_IDS_CHANGED = WifiManager.NETWORK_IDS_CHANGED_ACTION;
    private static final String ACTION_RSSI_CHANGED = WifiManager.RSSI_CHANGED_ACTION;
    private static final String ACTION_CONNECTIVITY = ConnectivityManager.CONNECTIVITY_ACTION;
    private Context appContext;
    //private static OnStartMonitorListener listener;
    private WifiRSSIMonitor rssiMonitor;
    private WifiMan wifiMan;
    private DialogMgr dialogMgr;

    public WifiActionReceiver(Context context, FragmentActivity activity)
    {
        this.appContext = context.getApplicationContext();
        wifiMan = WifiMan.getInstance(appContext);
        rssiMonitor = WifiRSSIMonitor.getInstance(appContext);
        dialogMgr = DialogMgr.getInstance(context, activity);
        System.out.println("-----wifiReceiver()----");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent)
    {

        String action = intent.getAction();

        //System.out.println("action--->"+action);

        if (action.equals(ACTION_WIFI_STATE_CHANGED)) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLING:
                    //System.out.println("com.ccit.dev.wifis state is enabling");

                    wifiMan.refreshWifiConState("正在打开...");

                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    System.out.println("---------wifi_state_enabled----------");
                    dialogMgr.dismissWifiDisabledHintDialog();
                    //System.out.println("com.ccit.dev.wifis state is enabled");
                    //                wifiMan.setWifiConnected(true);
                    wifiMan.refreshWifiConState("已打开");

                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    //System.out.println("com.ccit.dev.wifis state is disenabling");

                    wifiMan.refreshWifiConState("正在关闭...");

                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    System.out.println("---------wifi_state_disabled----------");
                    dialogMgr.showWifiDisabledHintDialog();
                    wifiMan.refreshWifiConState("未打开WLAN!");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    //System.out.println("com.ccit.dev.wifis state is unknow");

                    //wifiMan.refreshWifiConState("未知状态");
                    break;
                default:
                    break;
            }
        } else if (action.equals(ACTION_NETWORK_STATE_CHANGED)) {
            //		System.out.println("action--->ACTION_NETWORK_STATE_CHANGED");

            String BSSID = intent.getStringExtra(WifiManager.EXTRA_BSSID);
            String SSID = "";
            String ipAddr = "";
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Parcelable parcelableExtra2 = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

            if (parcelableExtra2 != null) {
                WifiInfo wifiInfo = (WifiInfo) parcelableExtra2;
                SSID = wifiInfo.getSSID();
                ipAddr = Formatter.formatIpAddress(wifiInfo.getIpAddress());
            }

            if (parcelableExtra != null) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;

                DetailedState detailedState = networkInfo.getDetailedState();

                switch (detailedState) {
                    case IDLE:
                        //System.out.println("******************idle*********************");
                        break;
                    case SCANNING:
                        //System.out.println("******************scanning*********************");
                        break;
                    case CONNECTING:
                        wifiMan.refreshWifiConState("正在连接...");
                        //System.out.println("******************connecting*********************");
                        break;
                    case AUTHENTICATING:
                        wifiMan.refreshWifiConState("正在获取IP地址...");
                        //System.out.println("******************authenticating*********************");
                        break;
                    case OBTAINING_IPADDR:
                        //System.out.println("******************obtaining_ipaddr*********************");
                        break;
                    case CONNECTED:
                        System.out.println("-----------connected-----------");
                        wifiMan.setWifiConnected(true);
                        wifiMan.refreshWifiConState(
                                "已连接" + ": " + SSID.substring(1, SSID.length() - 1) + " (" + BSSID +
                                ")" + "\n" + "IP地址: " + ipAddr);

                        //System.out.println("******************connected*********************");
                        break;
                    case SUSPENDED:
                        //System.out.println("******************suspended*********************");
                        break;
                    case DISCONNECTING:
                        wifiMan.refreshWifiConState("正在断开连接");
                        //System.out.println("******************disconnecting*********************");
                        break;
                    case DISCONNECTED:
                        System.out.println("-----------disconnected-----------");
                        wifiMan.setWifiConnected(false);
                        wifiMan.refreshWifiConState("未连接!");

                        //System.out.println("******************disconnected*********************");
                        break;
                    case FAILED:
                        //System.out.println("******************failed*********************");
                        break;
                    case BLOCKED:
                        //System.out.println("******************blocked*********************");
                        break;
                    case VERIFYING_POOR_LINK:
                        //System.out.println("******************verifying_poor_link*********************");
                        break;
                    case CAPTIVE_PORTAL_CHECK:
                        //System.out.println("******************captive_portal_check*********************");
                        break;
                    default:
                        break;
                }

            }

        } else if (action.equals(ACITON_SCAN_RESULTS_AVAILABLE)) {
            //			System.out.println("--------*****----------scan_results_available----------****--------");

            //设置新的扫描到的接入点数据
            wifiMan.setNewScanResultsData();

            //通知刷新Wifi接入点列表
            wifiMan.refreshWifiAPList();

            //通知刷新指定接入点信号值
            wifiMan.refreshSpecifiedAPRSSI();

            //通知重绘Wifi信号图
            rssiMonitor.notifyWifiDataChanged(wifiMan.getNewScanAPResuls());

        }

    }

}
