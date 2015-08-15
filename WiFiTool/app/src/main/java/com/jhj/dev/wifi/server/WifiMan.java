package com.jhj.dev.wifi.server;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 江华建
 */
public class WifiMan {
    private static WifiMan wifiLab;

    /**
     * Wifi信息改变监听器(接入点列表)
     */
    private OnWifiInfoChangedListener listener;

    /**
     * 指定接入点信号改变监听器(寻找Wifi)
     */
    private OnSpecifiedAPRSSIChangedListener specAPRSSIChangedListener;

    /**
     * 应用上下文
     */
    private Context appContext;

    /**
     * Wifi管理员
     */
    private WifiManager wifiManager;

    //    private ConnectivityManager connectivityManager;
    /**
     * Wifi是否已连接
     */
    private boolean isWifiConnected;
    //
    private String apConnectState = "";
    //	private NetworkInfo networkInfo;
    //
    //	private WifiInfo wifiInfo;
    //
    //	private DhcpInfo dhcpInfo;

    /**
     * 扫描后的接入点列表
     */
    private List<ScanResult> wifiScanResults;


    /**
     * 所有AP的MAC地址
     */
    private List<String> apBSSIDs;

    /**
     * 所有AP组的MAC地址
     */
    private List<String> apGroupBSSIDs;

    /**
     * 所有AP子组的MAC地址（不包括所属该组的值）
     */
    private List<LinkedList<String>> apChildList_BSSID;

    /**
     * 所有AP组的名字
     */
    private List<String> apGroupNames;

    /**
     * 所有AP子组（不包括所属该组的值）
     */
    private List<LinkedList<String>> apChildList_name;

    /**
     * 所有AP组的信号
     */
    private List<Integer> apGroupLevels;

    /**
     * 所有AP子组的信号（不包括所属该组的值）
     */
    private List<LinkedList<Integer>> apChildList_level;

    /**
     * 所有AP组的加密方式
     */
    private List<String> apGroupCapabilities;

    /**
     * 所有AP子组的加密方式（不包括所属该组的值）
     */
    private List<LinkedList<String>> apChildList_capabilities;


    /**
     * 所有接入点组的频率
     */
    private List<Integer> apGroupFrequency;

    /**
     * 所有接入点子组的频率
     */
    private List<LinkedList<Integer>> apChildList_frequency;

    /**
     * 接入点列表排序方法名
     */
    private String sortMethodName = "sortByWifiLevel";

    /**
     * 是否刷新Wifi接入点列表
     */
    private boolean isRefreshWifiAPList;

    /**
     * 是否刷新指定接入点信号
     */
    private boolean isRefreshSpecifiedAPRSSI;

    /**
     * 指定接入点MAC地址
     */
    private String specBSSID;

    /**
     * 指定接入点信号
     */
    private int specAPRSSI;


    public WifiMan(Context context) {
        this.appContext = context;

        wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        //        connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        apBSSIDs = new LinkedList<String>();
        apChildList_BSSID = new LinkedList<LinkedList<String>>();
        apGroupBSSIDs = new LinkedList<String>();

        apGroupNames = new LinkedList<String>();
        apChildList_name = new LinkedList<LinkedList<String>>();

        apGroupLevels = new LinkedList<Integer>();
        apChildList_level = new LinkedList<LinkedList<Integer>>();

        apGroupCapabilities = new LinkedList<String>();
        apChildList_capabilities = new LinkedList<LinkedList<String>>();

        apGroupFrequency = new LinkedList<Integer>();
        apChildList_frequency = new LinkedList<LinkedList<Integer>>();
        //		networkInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //		wifiInfo=wifiManager.getConnectionInfo();
        //		dhcpInfo=wifiManager.getDhcpInfo();


        //		System.out.println(wifiManager.getConfiguredNetworks().toString());
        //		System.out.println("netWorkInfo---->"+connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).toString());
        //		System.out.println("wifiInfo--->"+wifiManager.getConnectionInfo().toString());
        //		System.out.println("dhcpInfo--->"+wifiManager.getDhcpInfo().toString());
        //		System.out.println("ipAddress------------------>"+Formatter.formatIpAddress(wifiManager.getDhcpInfo().ipAddress));
        wifiScanResults = wifiManager.getScanResults();
        sortByWifiLevel(wifiScanResults);
        initData();

        System.out.println("firstScanResultSize-->" + wifiScanResults.size());

    }

    /**
     * 获取WifiMan实例对象引用
     *
     * @param context 应用上下文环境
     * @return WifiMan实例对象引用
     */
    public static WifiMan getInstance(Context context) {
        if (wifiLab == null) {
            wifiLab = new WifiMan(context.getApplicationContext());
        }
        return wifiLab;
    }

    /**
     * 设置Wifi信息改变监听器
     *
     * @param listener Wifi信息改变监听器
     */
    public void addOnWifiInfoChangedListener(OnWifiInfoChangedListener listener) {
        this.listener = listener;
    }

    /**
     * 添加监听指定接入点的信号改变监听器
     *
     * @param specAPRSSIChangedListener 指定接入点信号改变监听器
     */
    public void setOnSpecifiedAPRSSIChangedListener(
            OnSpecifiedAPRSSIChangedListener specAPRSSIChangedListener)
    {
        this.specAPRSSIChangedListener = specAPRSSIChangedListener;
    }

    /**
     * 初始化wifi接入点数据
     */
    private void initData() {
        refreshWifiAPData();
    }

    /**
     * 清除所有之前保存的接入点数据
     */
    public void clearAllData() {
        apBSSIDs.clear();
        apGroupBSSIDs.clear();
        apChildList_BSSID.clear();
        apGroupNames.clear();
        apChildList_name.clear();
        apGroupLevels.clear();
        apChildList_level.clear();
        apGroupFrequency.clear();
        apChildList_frequency.clear();
        apGroupCapabilities.clear();
        apChildList_capabilities.clear();

    }

    /**
     * 获取接入点的信号值
     *
     * @param isGroup   该接入点是否属于组
     * @param positions 接入点在二级列表里的索引位置
     * @return 接入点的信号值
     */
    public int getWifiAPRSSI(boolean isGroup, int... positions) {
        return isGroup ? apGroupLevels.get(positions[0])
                       : apChildList_level.get(positions[0]).get(positions[1]);
    }

    /**
     * 获取不同名的接入点数量
     *
     * @return 接入点列表不同名的接入点的数量
     */
    public int getWifiAPGroupCount() {
        return apGroupNames.size();
    }

    /**
     * 获取同名的接入点数量
     *
     * @param groupPosition Wifi接入点列表所属不同名的接入点所对应的索引位置
     * @return 接入点列表所属相同名字的接入点数量
     */
    public int getWifiAPChildCount(int groupPosition) {
        return apChildList_name.get(groupPosition).size();
    }

    /**
     * 获取不同名的接入点名字
     *
     * @param groupPosition Wifi接入点列表所属不同名的接入点所对应的索引位置
     * @return Wifi接入点所属不同名的接入点名字
     */
    public String getWifiAPGroupName(int groupPosition) {
        return apGroupNames.get(groupPosition);
    }

    /**
     * 获取相同名的接入点的名字
     *
     * @param groupPosition Wifi接入点列表所属不同名的接入点所对应的索引位置
     * @param childPosition Wifi接入点列表所属相同名的的索引位置
     * @return Wifi接入点所属相同名的接入点名字
     */
    public String getWifiAPChildName(int groupPosition, int childPosition) {
        return apChildList_name.get(groupPosition).get(childPosition);
    }

    /**
     * 获取接入点的MAC地址
     *
     * @param isGroup   该接入点是否属于组
     * @param positions 接入点在二级列表里的索引位置
     * @return 接入点的MAC地址
     */
    public String getWifiAPMAC(boolean isGroup, int... positions) {
        return isGroup ? apGroupBSSIDs.get(positions[0])
                       : apChildList_BSSID.get(positions[0]).get(positions[1]);
    }

    /**
     * 获取接入点的安全性信息
     *
     * @param isGroup   该接入点是否属于组
     * @param positions 接入点在二级列表里的索引位置
     * @return 接入点的安全性信息
     */
    public String getWifiAPCapabilities(boolean isGroup, int... positions) {
        return isGroup ? apGroupCapabilities.get(positions[0])
                       : apChildList_capabilities.get(positions[0]).get(positions[1]);
    }

    /**
     * 获取接入点的频率
     *
     * @param isGroup   该接入点是否属于组
     * @param positions 接入点在二级列表里的索引位置
     * @return 接入点频率
     */
    public int getWifiAPFrequency(boolean isGroup, int... positions) {
        return isGroup ? apGroupFrequency.get(positions[0])
                       : apChildList_frequency.get(positions[0]).get(positions[1]);
    }

        /**
         * 获取接入点的厂商名
         *
         * @param isGroup   该接入点是否属于组
         * @param positions 接入点在二级列表里的索引位置
         * @return 接入点的厂商名
         */
        public String getWifiAPOrganization(boolean isGroup, int... positions) {
            return isGroup ? doMACQuery(apGroupBSSIDs.get(positions[0]))
                           : doMACQuery(apChildList_BSSID.get(positions[0]).get(positions[1]));
        }

//    /**
//     * 获取接入点的厂商名
//     *
//     * @param isGroup   该接入点是否属于组
//     * @param positions 接入点在二级列表里的索引位置
//     * @return 接入点的厂商名
//     */
//    public String getWifiAPOrganization(boolean isGroup, int... positions) {
//        return isGroup ? appContext.getString(R.string.txt_unknown)
//                       : appContext.getString(R.string.txt_unknown);
//    }

    /**
     * 判断接入点是否含有子组
     *
     * @param groupPosition 接入点组的位置
     * @return 是否含有子组
     */
    public boolean isHasChildren(int groupPosition) {
        return apChildList_BSSID.get(groupPosition).size() > 0;
    }

    /**
     * 判断是否需要刷新Wifi接入点列表
     *
     * @return 是否要刷新Wifi接入点列表
     */
    public boolean isRefreshWifiAPList() {
        return isRefreshWifiAPList;
    }

    /**
     * 设置是否需要刷新Wifi接入点列表
     *
     * @param isRefreshWifiAPList 是否要刷新Wifi接入点列表
     */
    public void setRefreshWifiAPList(boolean isRefreshWifiAPList) {
        this.isRefreshWifiAPList = isRefreshWifiAPList;
    }

    /**
     * 判断接入点是否加密
     *
     * @param capabilities 接入点加密方式
     * @return 接入点是否加密
     */
    public boolean isWifiLocked(String capabilities) {
        boolean isWifiOpen = capabilities.equals("[ESS]") || capabilities.equals("[WPS][ESS]");
        return !isWifiOpen;
    }

    public int getNetworkId(String SSID) {
        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
            if (wifiConfiguration.SSID.equals(SSID)) {
                return wifiConfiguration.networkId;
            }
        }
        return -1;
    }

    public boolean isWifiConfigSaved(String SSID) {
        return getNetworkId(SSID) != -1;
    }

    public void connectWifi(String SSID, String pwd) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + SSID + "\"";
        configuration.preSharedKey = "\"" + pwd + "\"";
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.status = WifiConfiguration.Status.ENABLED;
        configuration.hiddenSSID = false;
        int networkId = wifiManager.addNetwork(configuration);
        wifiManager.enableNetwork(networkId, true);
    }

    public void connectWifiDirect(String SSID) {
        wifiManager.enableNetwork(getNetworkId(SSID), true);
    }

    /**
     * 判断Wifi是否可用
     *
     * @return wifi是否已打开
     */
    public boolean isWifiOpened() {
        return wifiManager.isWifiEnabled();
    }

    //	public boolean isOpenWifi(String SSID)
    //	{
    //		List<WifiConfiguration> wifiConfigurations=wifiManager.getConfiguredNetworks();
    //		for (WifiConfiguration wifiConfiguration : wifiConfigurations)
    //		{
    //			if (wifiConfiguration.SSID.equals(SSID)&&wifiConfiguratio)
    //			{
    //				return wifiConfiguration.networkId;
    //			}
    //		}
    //		return -1;
    //	}

    /**
     * 打开Wifi
     */
    public void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 设置新的扫描到的wifi接入点数据
     */
    @SuppressWarnings("unchecked")
    public void setNewScanResultsData() {
        //清空旧的扫描数据
        wifiScanResults.clear();
        try {
            //添加新的扫描到的数据
            wifiScanResults.addAll((List<ScanResult>) WifiMan.class
                    .getDeclaredMethod(sortMethodName, List.class)
                    .invoke(WifiMan.this, wifiManager.getScanResults()));
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    //	public void connectOpenWifiDirect()
    //	{
    //
    //	}

    /**
     * 改变接入点列表的排序方法
     *
     * @param sortName 需要排序名称
     */
    public void changeSort(String sortName) {
        sortMethodName = sortName;
    }

    /**
     * 获取新的扫描数据集
     *
     * @return 新的扫描结果集
     */
    public List<ScanResult> getNewScanAPResuls() {
        return wifiScanResults;
    }

    //	public boolean isAPConnected()
    //	{
    //		NetworkInfo networkInfo = connectivityManager
    //				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    //		if (networkInfo != null)
    //		{
    //			return networkInfo.isConnected();
    //		}
    //		return false;
    //	}

    /**
     * 判断是否已连接到某个接入点
     *
     * @return 是否已连接到某个接入点
     */
    public boolean isWifiConnected() {
        return isWifiConnected;
    }

    /**
     * 设置是否已连接到某个接入点
     *
     * @param isWifiConnected 是否已连接到某个接入点
     */
    public void setWifiConnected(boolean isWifiConnected) {
        this.isWifiConnected = isWifiConnected;
    }

    public String getApConnectState() {
        return apConnectState;
    }

    /**
     * 刷新wifi接入点数据
     */
    private void refreshWifiAPData() {
        for (ScanResult apResult : wifiScanResults) {
            String apBSSID = apResult.BSSID;
            String apName = apResult.SSID;
            int apLevel = apResult.level;
            String apCapabilities = apResult.capabilities;
            int apFrequency = apResult.frequency;
            //			System.out.println("apName------->"+apName);

            if (!apBSSIDs.contains(apBSSID)) {
                apBSSIDs.add(apBSSID);

                //				if (apName.equals("momo"))
                //				{
                //					System.out.println("apName----------->"+apName+" ,apRSSI------->"+apLevel);
                //				}

                //				System.out.println("+++++++++++++++++???+++++++++++++++++");
                //添加组
                if (!apGroupNames.contains(apName)) {
                    //System.out.println("apGroupNames not contains this name------>"+apName);

                    apGroupNames.add(apName);
                    LinkedList<String> apChildNames = new LinkedList<String>();
                    apChildList_name.add(apChildNames);

                    apGroupLevels.add(apLevel);
                    LinkedList<Integer> apChildLevel = new LinkedList<Integer>();
                    apChildList_level.add(apChildLevel);

                    apGroupCapabilities.add(apCapabilities);
                    LinkedList<String> apChildCapabilities = new LinkedList<String>();
                    apChildList_capabilities.add(apChildCapabilities);

                    apGroupBSSIDs.add(apBSSID);
                    LinkedList<String> apChildBSSID = new LinkedList<String>();
                    apChildList_BSSID.add(apChildBSSID);

                    apGroupFrequency.add(apFrequency);
                    LinkedList<Integer> apChildFrequency = new LinkedList<Integer>();
                    apChildList_frequency.add(apChildFrequency);

                    //添加子组
                } else {
                    int childListIndex = apGroupNames.indexOf(apName);
                    apChildList_name.get(childListIndex).add(apName);
                    apChildList_level.get(childListIndex).add(apLevel);
                    apChildList_capabilities.get(childListIndex).add(apCapabilities);
                    apChildList_BSSID.get(childListIndex).add(apBSSID);
                    apChildList_frequency.get(childListIndex).add(apFrequency);
                }
            } else {//更新组和子组的信号值
                System.out.println("*****************跟新*******************");
                //组 的索引位置
                int groupIndex = apGroupNames.indexOf(apName);
                //对应组的子的BSSID列表
                LinkedList<String> apChildBSSID = apChildList_BSSID.get(groupIndex);

                //判断该组是否为有子
                if (apChildList_name.get(groupIndex).size() > 0 && apChildBSSID.contains(apBSSID)) {
                    //System.out.println("groupIndex-->"+groupIndex+", apBSSIDIndex-->"+apChildBSSID.indexOf(apBSSID));
                    //设置第几组子的第几个索引位置的信号值
                    apChildList_level.get(groupIndex).set(apChildBSSID.indexOf(apBSSID), apLevel);
                } else {
                    //设置组的索引位置的信号值
                    apGroupLevels.set(groupIndex, apLevel);
                }

            }
        }
    }

    /**
     * 刷新Wifi接入点列表
     */
    public void refreshWifiAPList() {
        //如果需要刷新接入点列表就执行
        //1.清空所有旧的接入点数据
        //2.填充新的接入点数据
        //3.通知刷新接入点列表数据
        if (isRefreshWifiAPList && listener != null) {
            System.out.println("----------refreshWifiAPList-------------");
            clearAllData();
            refreshWifiAPData();
            listener.onWifiInfoChanged();
        }

    }

    /**
     * 获取所有接入点信息
     *
     * @return 包含所有接入点的名称和MAC地址
     */
    public String[] getApInfos() {
        String[] apInfos = new String[wifiScanResults.size()];
        for (int i = 0; i < wifiScanResults.size(); i++) {
            ScanResult scanResult = wifiScanResults.get(i);
            apInfos[i] = scanResult.SSID + "\n" + scanResult.BSSID;
        }
        return apInfos;
    }

    /**
     * 判断是否需要刷新指定接入点信号值
     *
     * @return 是否刷新指定接入点信号值
     */
    public boolean isRefreshSpecifiedAPRSSI() {
        return isRefreshSpecifiedAPRSSI;
    }

    /**
     * 设置是否需要刷新指定接入点信号值
     *
     * @param isRefreshSpecifiedAPRSSI 是否刷新指定接入点信号值
     */
    public void setRefreshSpecifiedAPRSSI(boolean isRefreshSpecifiedAPRSSI) {
        this.isRefreshSpecifiedAPRSSI = isRefreshSpecifiedAPRSSI;
    }

    /**
     * 获取指定接入点 的MAC地址
     *
     * @return 指定接入点的MAC地址
     */
    public String getSpecBSSID() {
        return specBSSID;
    }


    //	private String[] setNewAPInfos()
    //	{
    //		apInfos=new String[wifiScanResults.size()];
    //		for (int i = 0; i < wifiScanResults.size(); i++)
    //		{
    //			ScanResult scanResult=wifiScanResults.get(i);
    //			apInfos[i]=scanResult.SSID+"\n"+scanResult.BSSID;
    //		}
    //		return apInfos;
    //	}


    //--------------TODO-----------

    /**
     * 设置指定接入点的MAC地址
     *
     * @param specBSSID 指定接入点的MAC地址
     */
    public void setSpecBSSID(String specBSSID) {
        this.specBSSID = specBSSID;
    }

    /**
     * 判断当前是否拥有指定接入点
     *
     * @param specAPBSSID 指定接入点的MAC地址
     * @return 是否拥有指定接入点
     */
    private boolean hasSpecAPCurrently(String specAPBSSID) {
        for (ScanResult scanResult : wifiScanResults) {
            if (scanResult.BSSID.equals(specAPBSSID)) {
                specAPRSSI = scanResult.level;

                System.out.println(
                        "apName----->" + scanResult.SSID + " ,apBSSID---->" + scanResult.BSSID +
                        " ,apRSSI---->" + scanResult.level);

                return true;
            }
        }
        return false;
    }

    /**
     * 通过指定接入点MAC地址获取接入点信号值
     *
     * @param specAPBSSID 指定接入点的MAC地址
     * @return 指定接入点信号值
     */
    public int getAPRSSIBySpecBSSID(String specAPBSSID) {
        return hasSpecAPCurrently(specAPBSSID) ? specAPRSSI : -100;
    }

    /**
     * 刷新指定接入点信号值
     */
    public void refreshSpecifiedAPRSSI() {
        //如果需要刷新指定接入点信号值就通知刷新
        if (isRefreshSpecifiedAPRSSI && specAPRSSIChangedListener != null) {
            System.out.println("----------refreshSpecifiedAPRSSI-------------");
            specAPRSSIChangedListener
                    .onSpecifiedAPRSSIChanged(getAPRSSIBySpecBSSID(getSpecBSSID()));
        }
    }

    /**
     * 判断该接入点是否连接上路由
     *
     * @param apBSSID 接入点 MAC地址
     * @return 该接入点是否连接上路由
     */
    private boolean isAPConnected(String apBSSID) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        boolean b = wifiInfo.getBSSID() != null;
        System.out.println(
                "isAPConnected--->" + isWifiConnected + ", wifiInfo.getBSSID() != null--->" + b);
        return isWifiConnected && wifiInfo.getBSSID() != null &&
               wifiInfo.getBSSID().equals(apBSSID);
    }

        /**
         * MAC信息查询
         *
         * @param apMAC 指定MAC地址
         * @return 该接入点的厂商名
         */
        private String doMACQuery(String apMAC) {
            String mac = apMAC.trim().substring(0, 8).toUpperCase();
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase("/data/data/com.jhj.dev" +
                                                                          ".wifi" +
                                                                          "" +
                                                                          "" +
                                                                          "" +
                                                                          "" +
                                                                          ".server/databases/Points",
                                                                          null);
            Cursor cursor = database.rawQuery(
                    "select * from " + "MacInfoSql" +
                    " where " + "mac"+"=?",
                    new String[]{mac});
            String organization = "";
            if (cursor != null && cursor.moveToNext()) {
                organization = cursor.getString(cursor.getColumnIndex(
                        "manufacturer"));
            }
            if (cursor != null) {
                cursor.close();
            }
            database.close();
            return organization.equals("") ? appContext.getString(R.string.txt_unknown) : organization;
        }

    /**
     * 判断该接入点是否是5GHz路由
     *
     * @param isGroup   该接入点是否属于组
     * @param positions 该接入点在列表里的位置索引
     * @return 接入点是否是5GHz路由
     */
    public boolean isAP5GHz(boolean isGroup, int... positions) {
        int apFrequency = isGroup ? apGroupFrequency.get(positions[0])
                                  : apChildList_frequency.get(positions[0]).get(positions[1]);
        return apFrequency > 4900 && apFrequency < 5900;
    }

    /**
     * 判断接入点的频率所属类别
     *
     * @param frequency 接入点的频率
     * @return 接入点所属的频率类别
     */
    public String judgeFrequency(int frequency) {
        String frequencyType = "";
        if (frequency > 2400 && frequency < 2500) {
            frequencyType = "2.4";
        } else if (frequency > 4900 && frequency < 5900) {
            frequencyType = "5";
        }
        return frequencyType;
    }


    //	-------------------------------TODO--------------------------

    //	private boolean hasWifiAPCurrently(String apBSSID)
    //	{
    //		return apBSSIDs.contains(apBSSID);
    //	}

    /**
     * 获取所选的接入点的详情信息
     *
     * @param isGroup  该接入点是否属于组
     * @param position 接入点在列表里的位置索引
     * @return 所选的接入点的详情信息
     */
    public String getWifiAPSelectedDetails(boolean isGroup, int... position) {
        StringBuilder sb = new StringBuilder();
        try {
            String apMAC = isGroup ? apGroupBSSIDs.get(position[0])
                                   : apChildList_BSSID.get(position[0]).get(position[1]);

            boolean isAPConnected = isAPConnected(apMAC);

            int apFrequency = isGroup ? apGroupFrequency.get(position[0])
                                      : apChildList_frequency.get(position[0]).get(position[1]);

            sb.append(isAPConnected ? getWifiAPConnectedDetails() : "").append(isAPConnected ? ""
                                                                                             : appContext
                                                                                       .getString(
                                                                                               R.string.txt_server_mac))
              .append(isAPConnected ? "" : apMAC).append("\n")
              .append(appContext.getString(R.string.txt_security))
              .append(isGroup ? apGroupCapabilities.get(position[0])
                              : apChildList_capabilities.get(position[0]).get(position[1]))
              .append("\n").append(appContext.getString(R.string.txt_frequency))
              .append(apFrequency + appContext.getString(R.string.txt_MHz) +
                      judgeFrequency(apFrequency) +
                      appContext.getString(R.string.txt_GHz)).append("\n")
              .append(appContext.getString(R.string.txt_organization))
              .append(doMACQuery(apMAC));//doMacQuery
        } catch (Exception e) {
            return appContext.getString(R.string.txt_unknown);
        }

        return sb.toString();
    }
    //	public String getWifiAPBSSID(boolean isGroup, int... position)
    //	{
    //		return isGroup ? apGroupBSSIDs.get(position[0]) : apChildList_BSSID
    //				.get(position[0]).get(position[1]);
    //	}
    //	private boolean isAPGroup(String BSSID)
    //	{
    //		return apGroupBSSIDs.contains(BSSID);
    //	}

    /**
     * @return 获取设备所连接的接入点的详情信息
     */
    @SuppressWarnings("deprecation")
    public String getWifiAPConnectedDetails() {
        StringBuilder sb = new StringBuilder();
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

            sb.append(appContext.getString(R.string.txt_server_mac))
              .append(wifiInfo.getBSSID()).append("\n")
              .append(appContext.getString(R.string.txt_server_ip))
              .append(Formatter.formatIpAddress(dhcpInfo.serverAddress)).append("\n")
              .append(appContext.getString(R.string.txt_local_mac))
              .append(wifiInfo.getMacAddress()).append("\n")
              .append(appContext.getString(R.string.txt_local_ip))
              .append(Formatter.formatIpAddress(wifiInfo.getIpAddress())).append("\n")
              .append(appContext.getString(R.string.txt_gateway))
              .append(Formatter.formatIpAddress(dhcpInfo.gateway)).append("\n")
              .append(appContext.getString(R.string.txt_netmask))
              .append(Formatter.formatIpAddress(dhcpInfo.netmask)).append("\n")
              .append(appContext.getString(R.string.txt_DNS1))
              .append(Formatter.formatIpAddress(dhcpInfo.dns1)).append("\n")
              .append(appContext.getString(R.string.txt_DNS2))
              .append(Formatter.formatIpAddress(dhcpInfo.dns2)).append("\n")
              .append(appContext.getString(R.string.txt_hideSSID))
              .append(wifiInfo.getHiddenSSID() ? appContext
                      .getString(R.string.txt_yes) : appContext
                              .getString(R.string.txt_no)).append("\n")
              .append(appContext.getString(R.string.txt_leaseDuration))
              .append(dhcpInfo.leaseDuration)
              .append(appContext.getString(R.string.txt_second))
              .append(dhcpInfo.leaseDuration / 3600)
              .append(appContext.getString(R.string.txt_hour)).append("\n")
              .append(appContext.getString(R.string.txt_linkSpeed))
              .append(wifiInfo.getLinkSpeed()).append(" ").append(WifiInfo.LINK_SPEED_UNITS);

        } catch (Exception e) {
            return appContext.getString(R.string.txt_unknown);
        }

        return sb.toString();
    }

    /**
     * 按信号从大到小排序接入点列表
     *
     * @param wifiResults 扫描到的接入点集
     * @return 排序后的接入点集
     */
    private List<ScanResult> sortByWifiLevel(List<ScanResult> wifiResults) {
        Collections.sort(wifiResults, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return lhs.level > rhs.level ? -1 : 1;
            }

        });

        return wifiResults;
    }

    /**
     * 按Wifi名字字母顺序排序接入点列表
     *
     * @param wifiResults 扫描到的接入点集
     * @return 排序后的接入点集
     */
    @SuppressWarnings("unused")
    private List<ScanResult> sortByWifiName(List<ScanResult> wifiResults) {
        Collections.sort(wifiResults, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.SSID, rhs.SSID);
            }

        });

        return wifiResults;
    }

    /**
     * 刷新Wifi接入点连接状态
     *
     * @param wifiState wifi接入点的连接状态
     */
    public void refreshWifiConState(String wifiState) {
        if (listener != null) {
            listener.onWifiConStateChanged(wifiState);
            apConnectState = wifiState;
        }
    }

    /**
     * 获取所属不同名的接入点的wifi信号值所对应的wifi等级图标
     *
     * @param groupPosition Wifi接入点列表所属不同名的接入点所对应的索引位置
     * @return 所属不同名的接入点信号等级图标
     */
    public int getGroupWifiAPLevelIcon(int groupPosition) {
        return getLevelIcon(apGroupLevels.get(groupPosition),
                            isWifiLocked(apGroupCapabilities.get(groupPosition)));
    }

    /**
     * 获取所属相同名的接入点wifi信号对应的wifi等级图标
     *
     * @param groupPosition Wifi接入点列表所属不同名的接入点所对应的索引位置
     * @param childPosition Wifi接入点列表所属相同名的的索引位置
     * @return 所属相同名的接入点信号等级图标
     */
    public int getChildWifiAPLevelIcon(int groupPosition, int childPosition) {
        return getLevelIcon(apChildList_level.get(groupPosition).get(childPosition), isWifiLocked(
                apChildList_capabilities.get(groupPosition).get(childPosition)));
    }

    /**
     * 获取信号值对应的wifi等级图标
     *
     * @param level        信号值
     * @param isWifiLocked 接入点是否已加密
     * @return Wifi信号对应的Wifi等级图标
     */
    private int getLevelIcon(int level, boolean isWifiLocked) {
        int singleLevel = WifiManager.calculateSignalLevel(level, 4);
        switch (singleLevel) {
            case 3:
                return isWifiLocked ? R.drawable.ic_wifi_lock_signal_4
                                    : R.drawable.ic_wifi_signal_4;
            case 2:
                return isWifiLocked ? R.drawable.ic_wifi_lock_signal_3
                                    : R.drawable.ic_wifi_signal_3;
            case 1:
                return isWifiLocked ? R.drawable.ic_wifi_lock_signal_2
                                    : R.drawable.ic_wifi_signal_2;
            case 0:
                return isWifiLocked ? R.drawable.ic_wifi_lock_signal_1
                                    : R.drawable.ic_wifi_signal_1;
            default:
                break;
        }
        return 0;
    }

    /**
     * 重新扫描周围接入点
     */
    public void reScan() {
        wifiManager.startScan();

    }

    /**
     * Wifi信息改变监听接口
     */
    public interface OnWifiInfoChangedListener {
        /**
         * Wifi信息改变回调方法
         */
        void onWifiInfoChanged();

        /**
         * Wifi连接状态改变回调
         *
         * @param newConState 新连接状态
         */
        void onWifiConStateChanged(String newConState);
    }

    /**
     * 指定接入点信号改变监听接口
     */
    public interface OnSpecifiedAPRSSIChangedListener {
        //指定接入点信号改变回调
        void onSpecifiedAPRSSIChanged(int specAPRSSI);

    }

}
