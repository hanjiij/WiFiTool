package com.jhj.dev.wifi.server.model;

import android.content.Context;
import android.net.wifi.ScanResult;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author 江华健
 */
public class WifiMapData {
    private static WifiMapData mapData;
    /**
     * 是否过滤
     */
    public boolean isFilter = false;
    private Context appContext;
    /**
     * 信号监听器
     */
    private OnRSSIChangedListener rssiChangedListener;
    /**
     * 接入点列表
     */
    private List<LinkedList<Integer>> apList;
    /**
     * 接入点MAC地址
     */
    private List<String> apBSSIDs;
    /**
     * 接入点名字
     */
    private List<String> apNames;
    /**
     * 之前过滤后的接入点MAC地址集
     */
    private Set<String> beforeFilteredAPBSSIDs;
    /**
     * 要保留的接入点MAC地址集
     */
    private List<String> retainedAPBSSIDs;
    /**
     * 要保留的接入点列表
     */
    private List<LinkedList<Integer>> retainedAPs;
    /**
     * 要保留的接入点名字集
     */
    private List<String> retainedAPNames;
    /**
     * 过滤后的信号地图数据
     */
    private WifiFilteredMapData filteredMapData;

    public WifiMapData(Context context)
    {
        this.appContext = context;
        filteredMapData = WifiFilteredMapData.getInstance(appContext);

        apList = Collections.synchronizedList(new LinkedList<LinkedList<Integer>>());
        apBSSIDs = Collections.synchronizedList(new LinkedList<String>());
        apNames = Collections.synchronizedList(new LinkedList<String>());
        beforeFilteredAPBSSIDs = Collections.synchronizedSet(new LinkedHashSet<String>());

        retainedAPBSSIDs = new LinkedList<String>();
        retainedAPs = new LinkedList<LinkedList<Integer>>();
        retainedAPNames = new LinkedList<String>();
    }

    public static WifiMapData getInstance(Context context)
    {
        if (mapData == null) {
            mapData = new WifiMapData(context.getApplicationContext());
        }
        return mapData;
    }

    /**
     * 设置信号改变监听器
     *
     * @param rssiChangedListener 信号改变监听器
     */
    public void setOnRSSIChangedListener(OnRSSIChangedListener rssiChangedListener)
    {
        this.rssiChangedListener = rssiChangedListener;
    }

    /**
     * 获取之前过滤后的接入点MAC地址集
     */
    public Set<String> getBeforeFilteredAPBSSIDs()
    {
        return beforeFilteredAPBSSIDs;
    }

    /**
     * 刷新Wifi接入点数据
     *
     * @param newScanResult 新的扫描后的接入点集
     */
    public synchronized void refreshWifiAPData(List<ScanResult> newScanResult)
    {
        for (ScanResult scanResult : newScanResult) {
            if (!hasAPBefore(scanResult)) {
                LinkedList<Integer> ap = new LinkedList<Integer>();
                ap.add(scanResult.level);
                apBSSIDs.add(scanResult.BSSID);
                apList.add(ap);
                apNames.add(scanResult.SSID);

                if (scanResult.SSID.equals("NETGEAR57")) {
                    System.out.println("NETGEAR57-ssid---------------->" + scanResult.level);
                } else if (scanResult.SSID.equals("SA225-3")) {
                    System.out.println("SA225-3-ssid---------------->" + scanResult.level);
                }

            } else {
                int apIndex = apBSSIDs.indexOf(scanResult.BSSID);

                if (apList.get(apIndex).size() > 61) {
                    apList.get(apIndex).removeFirst();
                }

                apList.get(apIndex).add(scanResult.level);

                if (scanResult.SSID.equals("NETGEAR57")) {
                    System.out.println("NETGEAR57-ssid---->" + scanResult.level);
                } else if (scanResult.SSID.equals("SA225-3")) {
                    System.out.println("SA225-3-ssid---------------->" + scanResult.level);
                }
            }
        }

        if (isFilter) {
            filterWifi();

        } else {
            setWifiMapData(apBSSIDs, apList, apNames);
        }

        rssiChangedListener.onRSSIChanged(filteredMapData.getFilteredAPList());
    }

    /**
     * 判断是否过滤Wifi
     *
     * @return 是否过滤Wifi
     */
    public boolean isFilter()
    {
        return isFilter;
    }

    /**
     * 设置是否过滤接入点
     *
     * @param isFilter 是否过滤
     */
    public void setFilter(boolean isFilter)
    {
        this.isFilter = isFilter;
    }

    /**
     * 过滤Wifi
     */
    public void filterWifi()
    {
        retainedAPBSSIDs.clear();
        retainedAPs.clear();
        retainedAPNames.clear();

        for (String filteredAPBSSID : beforeFilteredAPBSSIDs) {
            int apIndex = apBSSIDs.indexOf(filteredAPBSSID);

            if (apIndex != -1) {
                retainedAPBSSIDs.add(apBSSIDs.get(apIndex));
                retainedAPs.add(apList.get(apIndex));
                retainedAPNames.add(apNames.get(apIndex));
            }
        }

        setWifiMapData(retainedAPBSSIDs, retainedAPs, retainedAPNames);

    }

    /**
     * 判断之前是否已经存储了该接入点
     *
     * @param scanResult 扫描到的接入点集
     * @return 是否已经存储了该接入点
     */
    public boolean hasAPBefore(ScanResult scanResult)
    {
        String BSSID = scanResult.BSSID;

        for (String apBBSSID : apBSSIDs) {
            if (BSSID.equals(apBBSSID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 设置Wifi信号地图数据
     *
     * @param apBSSIDs 过滤后的接入点MAC地址集
     * @param apList   过滤后的接入点列表
     * @param apNames  过滤后的接入点名称集
     */
    private void setWifiMapData(List<String> apBSSIDs, List<LinkedList<Integer>> apList,
                                List<String> apNames)
    {
        filteredMapData.setFilteredAPBSSIDs(apBSSIDs);

        filteredMapData.setFilteredAPList(apList);

        filteredMapData.setFilteredAPNames(apNames);
    }


    /**
     * 信号改变监听器接口
     */
    public interface OnRSSIChangedListener {
        /**
         * 当接入点信号值改变时回调该方法
         *
         * @param apList 接入点列表
         */
        void onRSSIChanged(List<LinkedList<Integer>> apList);
    }
}
