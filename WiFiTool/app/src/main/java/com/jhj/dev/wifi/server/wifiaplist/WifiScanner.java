package com.jhj.dev.wifi.server.wifiaplist;

import android.content.Context;

import com.jhj.dev.wifi.server.WifiMan;

/**
 * @author 江华健
 */
public class WifiScanner {
    private static WifiScanner wifiScanner;
    /**
     * 应用上下文
     */
    private Context appContext;
    /**
     * 是否扫描
     */
    private boolean isScan;
    /**
     * Wifi管理员
     */
    private WifiMan wifiMan;

    public WifiScanner(Context context)
    {
        appContext = context;
        wifiMan = WifiMan.getInstance(appContext);
    }

    public static WifiScanner getInstance(Context context)
    {
        if (wifiScanner == null) {
            wifiScanner = new WifiScanner(context.getApplicationContext());
        }
        return wifiScanner;
    }

    /**
     * 开始扫描
     */
    public void startScan()
    {
        isScan = true;
        new Thread(new Scanner()).start();
    }

    /**
     * 停止扫描
     */
    public void stopScan()
    {
        isScan = false;
    }

    /**
     * 接入点扫描任务
     */
    private class Scanner implements Runnable {
        @Override
        public void run()
        {
            while (isScan) {
                try {
                    System.out.println("------------Scanner-----------");
                    Thread.sleep(3000);
                    wifiMan.reScan();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
