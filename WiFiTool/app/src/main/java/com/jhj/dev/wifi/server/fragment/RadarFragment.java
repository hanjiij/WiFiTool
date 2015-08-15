package com.jhj.dev.wifi.server.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.mydatabase.MyDatabase;
import com.jhj.dev.wifi.server.myview.Radar_Draw_View;
import com.jhj.dev.wifi.server.util.GetLocation;
import com.jhj.dev.wifi.server.util.GetSensorInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author 韩吉
 */
@SuppressLint({"HandlerLeak", "InflateParams"})
@SuppressWarnings("deprecation")
public class RadarFragment extends Fragment {

    ImageView imageView;
    TextView angleTextView;
    Radar_Draw_View background;
    List<ScanResult> list;
    WifiManager wifiManager;
    float[] points;
    String[] name;
    Display mDisplay;
    int center;
    int k = 1;
    Timer timer;
    Handler handler;
    double point, angle = 0;
    Map<String, Double> map = new HashMap<>();
    GetSensorInfo getSensorInfo;
    GetLocation getLocation;
    SQLiteDatabase db;
    SharedPreferences sharedPreferences;
    LatLng aLatLng;
    String cityaddString = "未知";
    View view;
    private long fristtime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        setHasOptionsMenu(true);
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub

        if (view == null) {
            view = inflater.inflate(R.layout.activity_radar, null);
        } else {
            ((FrameLayout) view.getParent()).removeView(view);
        }

        init();

        setListener();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                if (msg.what == 1) {
                    initpoint();
                    background.flush(points, name);
                }
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }, 0, 3000);

        drawradar();

        return view;
    }

    /**
     * 初始化
     */
    private void init() {
        // TODO Auto-generated method stub
        mDisplay = getActivity().getWindowManager().getDefaultDisplay();
        center = mDisplay.getWidth() / 2;
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        imageView = (ImageView) view.findViewById(R.id.zoomgap);
        background = (Radar_Draw_View) view.findViewById(R.id.Drawimage);
        angleTextView = (TextView) view.findViewById(R.id.angleTextView);

        wifiManager.startScan();
        list = wifiManager.getScanResults();

        getSensorInfo = new GetSensorInfo(getActivity());
        getLocation = new GetLocation(getActivity());

        initAngle();
    }

    /**
     * 传感器监听函数
     */
    private void setListener() {

        getLocation.setOnLocationListener(
                new GetLocation.OnLocationListener() { // GPS的位置数据，定位成功一次后取消GPS定位

                    @Override
                    public void onLocationChanged(BDLocation bdLocation) {
                        // TODO Auto-generated method stub

                        cityaddString = bdLocation.getAddrStr();

                        getLocation.StopLocation();
                    }
                });

        getSensorInfo.setOnAcceleratedListener(new GetSensorInfo.OnAcceleratedListener() { // 加速度回调

            @Override
            public void onAcceleratedChanged(int count) {
                // TODO Auto-generated method stub

            }
        });

        getSensorInfo
                .setOnOrientationListener(new GetSensorInfo.OnOrientationListener() { // 方向传感器回调

                    @Override
                    public void onOrientationChanged(float orientation) {
                        // TODO Auto-generated method stub

                        if (Math.abs(angle - orientation) > 1 &&
                            (System.currentTimeMillis() - fristtime) > 200)
                        {

                            angle = orientation;

                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);

                            fristtime = System.currentTimeMillis();
                        }

                        if (orientation < 40) {

                            angleTextView
                                    .setText((int) orientation + "°  N" + "\n" + cityaddString);

                        } else if (orientation < 50) {

                            angleTextView
                                    .setText((int) orientation + "°  NE" + "\n" + cityaddString);

                        } else if (orientation < 120) {

                            angleTextView
                                    .setText((int) orientation + "°  E" + "\n" + cityaddString);

                        } else if (orientation < 150) {

                            angleTextView
                                    .setText((int) orientation + "°  SE" + "\n" + cityaddString);

                        } else if (orientation < 220) {

                            angleTextView
                                    .setText((int) orientation + "°  S" + "\n" + cityaddString);

                        } else if (orientation < 240) {

                            angleTextView
                                    .setText((int) orientation + "°  SW" + "\n" + cityaddString);

                        } else if (orientation < 300) {

                            angleTextView
                                    .setText((int) orientation + "°  W" + "\n" + cityaddString);

                        } else if (orientation < 330) {

                            angleTextView
                                    .setText((int) orientation + "°  NW" + "\n" + cityaddString);

                        } else {
                            angleTextView
                                    .setText((int) orientation + "°  N" + "\n" + cityaddString);
                        }

                    }
                });
    }

    /**
     * 初始化角度，为wifi的bssid指定唯一的角度
     */
    private void initAngle() {

        sharedPreferences = getActivity()
                .getSharedPreferences("local", Context.MODE_PRIVATE); // 用来存储上次退出时的位置，避免刚进入软件是定位在北京

        if (sharedPreferences.getFloat("latitude", 0) != 0) {

            aLatLng = new LatLng(sharedPreferences.getFloat("latitude", 0),
                                 sharedPreferences.getFloat("longitude", 0));

            map.clear();

            db = new MyDatabase(getActivity(), "Points").getWritableDatabase();

            Cursor cursor =
                    db.query("PointsSql", new String[]{"mac", "Lat", "Lng"}, null, null, null, null,
                             null);

            while (cursor.moveToNext()) {

                double angle = computingAngle(new LatLng(aLatLng.latitude, aLatLng.longitude),
                                              new LatLng(Double.parseDouble(cursor.getString(1)),
                                                         Double.parseDouble(cursor.getString(2))));

                map.put(cursor.getString(0), angle);
            }

            db.close();
        } else {

            for (int i = 0; i < list.size(); i++) { // 初始化角度

                map.put(list.get(i).BSSID, (double) (Math.random() * 360));
            }
        }
    }

    /**
     * 初始化点的位置信息和名称
     */
    private void initpoint() {
        // TODO Auto-generated method stub

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        wifiManager.startScan();
        list = wifiManager.getScanResults();

        points = new float[list.size() * 2];
        name = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {

            int level = -(list.get(i).level);

            if (map.get(list.get(i).BSSID) == null) {
                map.put(list.get(i).BSSID, Math.random() * 360);
                point = (double) map.get(list.get(i).BSSID) - angle;
            } else {
                point = (double) map.get(list.get(i).BSSID) - angle;
            }

            if (level < 50) {
                points[i * 2] = (float) (((level * 33 * center * 2) / (50 * 560)) *
                                         Math.cos(Math.toRadians(point))) + center;
                points[i * 2 + 1] = (float) (((level * 33 * center * 2) / (50 * 560)) *
                                             Math.sin(Math.toRadians(point))) + center;
            } else {
                points[i * 2] = (float) ((((level - 50) * 33 * center * 2) / (10 * 560) +
                                          center * 2 * 33 / (560 * 50)) *
                                         Math.cos(Math.toRadians(point))) + center;
                points[i * 2 + 1] = (float) ((((level - 50) * 33 * center * 2) / (10 * 560) +
                                              center * 2 * 33 / (560 * 50)) *
                                             Math.sin(Math.toRadians(point))) + center;
            }
            name[i] = list.get(i).SSID;
        }
    }

    /**
     * 根据经纬度的来计算角度
     *
     * @param A 雷达的当前位置
     * @param B wifi的位置
     * @return wifi相对于雷达中心的角度
     */
    private double computingAngle(LatLng A, LatLng B) { // 上北左西

        double angle = 0;

        double ya = A.latitude;
        double xa = A.longitude;

        double yb = B.latitude;
        double xb = B.longitude;

        if (xa <= xb && ya <= yb) { // 一
            angle = -Math.atan(Math.abs(ya - yb) / Math.abs(xa - xb)) / Math.PI * 180;

        }

        if (xa < xb && ya > yb) { // 四
            angle = 90 - Math.atan(Math.abs(xa - xb) / Math.abs(ya - yb)) / Math.PI * 180;

        }

        if (xa > xb && ya < yb) { // 二
            angle = Math.atan(Math.abs(ya - yb) / Math.abs(xa - xb)) / Math.PI * 180 + 180;

        }

        if (xa > xb && ya > yb) { // 三
            angle = Math.atan(Math.abs(xa - xb) / Math.abs(ya - yb)) / Math.PI * 180 + 90;

        }

        return angle;
    }

    /**
     * 设置雷达旋转的动画
     */
    private void drawradar() {
        // TODO Auto-generated method stub

        RotateAnimation rotateAnimation =
                new RotateAnimation(0, 360, mDisplay.getWidth() / 2, mDisplay.getWidth() / 2);
        rotateAnimation.setDuration(2000);
        rotateAnimation.setRepeatCount(-1);
        LinearInterpolator lin = new LinearInterpolator();// 设置匀速转动
        rotateAnimation.setInterpolator(lin);
        imageView.startAnimation(rotateAnimation);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        getSensorInfo.StartSensor();
        getLocation.StartLocation();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        getSensorInfo.StopSensor();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.radar, menu);

    }


}
