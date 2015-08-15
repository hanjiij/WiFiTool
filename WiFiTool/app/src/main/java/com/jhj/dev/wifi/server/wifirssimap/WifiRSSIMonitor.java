package com.jhj.dev.wifi.server.wifirssimap;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.WifiMan;
import com.jhj.dev.wifi.server.model.WifiMapData;

import java.util.List;

/**
 * @author 江华健
 */
public class WifiRSSIMonitor {
    private static WifiRSSIMonitor rssiMonitor;

    /**
     * 应用上下文
     */
    private Context appContext;
    /*
     * 是否扫描
     * */
    private boolean isScan = false;

    /*
     *扫描间隔等级
     **/
    private int scanInterverLv = 2;

    /*
     * 扫描间隔数组
     * */
    private int[] scanInterver;
    /*
     * 信号地图数据提供者
     * */
    private WifiMapData mapData;

    /*
     * wifi管理者
     * */
    private WifiMan wifiMan;

    public WifiRSSIMonitor(Context context)
    {
        this.appContext = context;
        wifiMan = WifiMan.getInstance(appContext);
        mapData = WifiMapData.getInstance(appContext);
        scanInterver = context.getResources().getIntArray(R.array.wifiScanInterver);
    }


    public static WifiRSSIMonitor getInstance(Context context)
    {
        if (rssiMonitor == null) {
            rssiMonitor = new WifiRSSIMonitor(context.getApplicationContext());
        }

        return rssiMonitor;
    }

    /**
     * 获取扫描间隔等级
     *
     * @return 扫描间隔等级
     */
    public int getScanInterverLv()
    {
        return scanInterverLv;
    }

    /**
     * 设置扫描等级
     *
     * @param scanInterver 扫描间隔
     */
    public void setScanInterverLv(int scanInterver)
    {
        this.scanInterverLv = scanInterver;
    }

    /**
     * 启动扫描信号任务
     */
    public void startMonitor()
    {
        isScan = true;
        new Thread(new RSSIScanTask()).start();

    }

    /**
     * 停止扫描任务
     */
    public void stopMonitor()
    {
        isScan = false;
    }

    /**
     * 通知wifi数据已改变
     *
     * @param newScanResult 新的扫描到的接入点集
     */
    public void notifyWifiDataChanged(List<ScanResult> newScanResult)
    {

        if (isScan) {
            System.out.println("----------notifyWifiDataChanged-------------");
            mapData.refreshWifiAPData(newScanResult);
        }

    }

    /**
     * 扫描信号任务
     */
    private class RSSIScanTask implements Runnable {

        @Override
        public void run()
        {
            while (isScan) {
                try {
                    System.out.println("------------RSSIScanTask-----------");
                    wifiMan.reScan();
                    Thread.sleep(scanInterver[scanInterverLv]);

                } catch (InterruptedException e) {

                    e.printStackTrace();
                }

            }

        }

    }
}
