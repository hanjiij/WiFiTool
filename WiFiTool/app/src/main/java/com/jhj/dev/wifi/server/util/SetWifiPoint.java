package com.jhj.dev.wifi.server.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.jhj.dev.wifi.server.mydatabase.MyDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 韩吉
 */

@SuppressWarnings("rawtypes")
public class SetWifiPoint {

    private Context context;
    private GetSensorInfo getSensorInfo;
    private GetLocation getLocation;
    private List<Map<String, Double>> points = new ArrayList<>(); // 行走的坐标集合
    private List<List<ScanResult>> wifiLists = new ArrayList<>(); // 每个坐标点的信号强度集合
    private Set<String> SetMac = new HashSet<>(); // 收集所有的wifi mac地址作为查询依据
    private WifiManager wifiManager;
    private int stature; // 人的身高
    private float orientation1; // 当前手机的方向
    private BDLocation Location;
    private SQLiteDatabase db;
    private double distance; // 单位经纬度的距离 0.000001经度
    private LatLng p1, p2;
    private Iterator it; // set的输出
    private int MAXlevel; // 记录每次查询的最强信号强度
    private SharedPreferences sharedPreferences;

    private IsLocationListener isLocationListener;

    public SetWifiPoint(Context context, int stature) {
        // TODO Auto-generated constructor stub

        this.context = context;
        this.stature = stature;
        init();
        setSensorlistener();
    }

    /**
     * 初始化各变量
     */
    private void init() {

        getSensorInfo = new GetSensorInfo(context);
        getLocation = new GetLocation(context);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 传感器的参数值获取
     */
    private void setSensorlistener() {

        getSensorInfo.setOnAcceleratedListener(new GetSensorInfo.OnAcceleratedListener() {

            @Override
            public void onAcceleratedChanged(int count) {
                // TODO Auto-generated method stub

                if (points.size() == 0) {
                    Map<String, Double> map = new HashMap<>();
                    map.put("X",
                            ((stature - 155.911) / 26.4) * Math.cos(Math.toRadians(orientation1)));
                    map.put("Y",
                            ((stature - 155.911) / 26.4) * Math.sin(Math.toRadians(orientation1)));
                    points.add(map);
                } else {

                    double x = points.get(points.size() - 1).get("X");
                    double y = points.get(points.size() - 1).get("Y");
                    Map<String, Double> map = new HashMap<>();
                    map.put("X",
                            ((stature - 155.911) / 26.4) * Math.cos(Math.toRadians(orientation1)) +
                            x);
                    map.put("Y",
                            ((stature - 155.911) / 26.4) * Math.sin(Math.toRadians(orientation1)) +
                            y);
                    points.add(map);
                }

                wifiManager.startScan();
                wifiLists.add(wifiManager.getScanResults());
                setAllMac(wifiManager.getScanResults());

            }
        });

        getSensorInfo
                .setOnOrientationListener(new GetSensorInfo.OnOrientationListener() { // 方向传感器的返回值

                    @Override
                    public void onOrientationChanged(float orientation) {
                        // TODO Auto-generated method stub

                        orientation1 = orientation;
                    }
                });

        getLocation.setOnLocationListener(new GetLocation.OnLocationListener() { // 定位，第一次定位后解除定位

            @Override
            public void onLocationChanged(BDLocation bdLocation) {
                // TODO Auto-generated method stub
                Location = bdLocation;

                SDKInitializer.initialize(context);

                p1 = new LatLng(Location.getLatitude(), Location.getLongitude());

                p2 = new LatLng(Location.getLatitude() + 1, Location.getLongitude());
                distance = DistanceUtil.getDistance(p1, p2);

                getLocation.StopLocation(); // 解除定位

                getSensorInfo.StartSensor(); // 开启加速度传感器

                isLocationListener.isLocationSuccess(true); // 传值定位成功

                sharedPreferences = context.getSharedPreferences( // 将位置记录到本地
                                                                  "local", Context.MODE_APPEND);

                Editor editor = sharedPreferences.edit();
                editor.putFloat("latitude", (float) bdLocation.getLatitude());
                editor.putFloat("longitude", (float) bdLocation.getLongitude());
                editor.commit();
            }
        });
    }

    public void stopGPSLocation() {
        getLocation.StopLocation(); // 解除定位
    }

    /**
     * @param list 将list里的所有mac地址存储到set集合中
     */
    private void setAllMac(List<ScanResult> list) {

        for (ScanResult scanResult : list) {
            SetMac.add(scanResult.BSSID);
        }
    }

    /**
     * @param mac 指定ssid的名字
     * @return 返回在第几步中出现最强信号，未找到返回-1
     */
    private int getLevelMAX(String mac) {

        MAXlevel = Integer.MIN_VALUE;
        int count = -1;

        for (int i = 0; i < wifiLists.size(); i++) {

            if (MAXlevel < getLevel(i, mac)) {
                MAXlevel = getLevel(i, mac);
                count = i;
            }
        }
        return count;
    }

    /**
     * @param count 需要获取第几步里的wifi集合
     * @param mac  指定获取哪个信号的强度
     * @return 返回信号的强度
     */
    private int getLevel(int count, String mac) {

        for (int i = 0; i < wifiLists.get(count).size(); i++) {
            ScanResult scanRet = wifiLists.get(count).get(i);
            if (scanRet.BSSID.equals(mac)) {
                return scanRet.level;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * @param mac mac地址
     * @return 返回mac地址所对应的ssid
     */
    private String MactoSsid(String mac) {
        for (int i = 0; i < wifiLists.size(); i++) {
            for (int j = 0; j < wifiLists.get(i).size(); j++) {
                ScanResult scanRet = wifiLists.get(i).get(j);
                if (scanRet.BSSID.equals(mac)) {
                    return scanRet.SSID;
                }
            }
        }

        return null;
    }

    /**
     * 开启传感器，开始记录
     */
    public void StartSetWifiPoint() {
        getLocation.StartLocation();
    }

    /**
     * 计算结果，存储到数据库中，并结束传感器 。判断是否走动，未走动则toast提示
     */
    public void SetSalculate() {

        if (wifiLists.size() != 0) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    db = new MyDatabase(context, "Points").getWritableDatabase();

                    getSensorInfo.StopSensor();

                    it = SetMac.iterator();

                    while (it.hasNext()) // 遍历所有的信号
                    {

                        String macString = it.next().toString();
                        int count = getLevelMAX(macString);
                        ContentValues values = new ContentValues();

                        if (judgeLevel(macString, MAXlevel) == 0) {

                            values.put("ssid", MactoSsid(macString));
                            values.put("mac", macString);
                            values.put("Lat", ToCoordinateLat(points.get(count).get("X")) + "");
                            values.put("Lng", ToCoordinateLon(points.get(count).get("Y")) + "");
                            values.put("Level", MAXlevel + "");
                            values.put("address", Location.getAddrStr());
                            db.insert("PointsSql", null, values);

                        } else if (judgeLevel(macString, MAXlevel) == 1) {

                            values.put("Level", MAXlevel + "");
                            db.update("PointsSql", values, "mac=?", new String[]{macString});
                        }
                    }

                    ContentValues values = new ContentValues();

                    values.put("address", Location.getAddrStr());
                    values.put("Lat", Location.getLatitude() + "");
                    values.put("Lng", Location.getLongitude() + "");

                    db.insert("HistoryPointsSql", null, values);

                    points.clear();
                    wifiLists.clear();
                    db.close();

                }
            }).start();

        } else {

            getSensorInfo.StopSensor();
            Toast.makeText(context, "您还未走动", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * @param macString
     * @param level
     * @return 数据库中找不到此mac则返回0，找到且信号值小于现有的则返回1，否则返回-1
     */
    private int judgeLevel(String macString, int level) {

        db = new MyDatabase(context, "Points").getWritableDatabase();

        Cursor cursor =
                db.query("PointsSql", new String[]{"mac", "Level"}, null, null, null, null, null);

        while (cursor.moveToNext()) {

            if (cursor.getString(0).equals(macString) &&
                level > Integer.parseInt(cursor.getString(1)))
            {
                return 1;
            } else if (cursor.getString(0).equals(macString)) {
                return -1;
            }
        }

        return 0;
    }

    /**
     * @param x X轴的距离
     * @return 返回X的纬度
     */
    public double ToCoordinateLat(double x) {

        double lat = Location.getLatitude() + x / distance;

        return lat;
    }

    /**
     * @param y Y轴的距离
     * @return 返回Y的经度
     */
    public double ToCoordinateLon(double y) {

        double lon = Location.getLongitude() + y / distance;

        return lon;
    }

    public void setIsLocationListener(IsLocationListener isLocationListener) {
        this.isLocationListener = isLocationListener;
    }

    public interface IsLocationListener {
        void isLocationSuccess(boolean isSuccess);
    }
}
