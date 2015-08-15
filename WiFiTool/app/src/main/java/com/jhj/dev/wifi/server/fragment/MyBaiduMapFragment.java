package com.jhj.dev.wifi.server.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.mydatabase.MyDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 韩吉
 */
@SuppressLint({"ShowToast", "InflateParams"})
public class MyBaiduMapFragment extends Fragment implements SensorEventListener {

    MapView mapView;
    BaiduMap baiduMap;
    ImageView locationbt, zoominbt, zoomoutbt;
    Marker marker;
    InfoWindow mInfoWindow;
    SDKReceiver mReceiver;
    LocationClient locationClient;
    BDLocation bdlocation;
    BDLocationListener locationListener;
    boolean isFirstLoc = true;
    float lastX = 0;
    ArrayList<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
    HashMap<String, Object> lin;

    SensorManager sensorManager;
    Sensor sensor;

    SharedPreferences sharedPreferences; // 记录最近一次的定位位置

    LatLng point;
    Display mDisplay;

    SQLiteDatabase db;

    //	View view;

    /**
     * 判断GPS是否开启，GPS和AGPS同时开启才认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps && network) {
            return true;
        }

        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        regeistbroadcast(); // 注册广播
        SDKInitializer.initialize(getActivity().getApplicationContext()); // 初始化地图

        System.out.println("onCreate");
        // if (!getActivity().getIntent().getBooleanExtra("isFirstLocExtra",
        // true)) { // 判断是否由其他页面打开地图
        // isFirstLoc = getActivity().getIntent().getBooleanExtra(
        // "isFirstLocExtra", true);
        //
        // // getActivity().getIntent().removeExtra("isFirstLocExtra");
        // System.out.println("onCreate执行了");
        // }
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub

        View view = inflater.inflate(R.layout.activity_baidu_map, null);
        //		if (view == null) {
        //			view =
        //		} else {
        //			((FrameLayout) view.getParent()).removeView(view);
        //		}

        if (!isOPen(getActivity().getApplicationContext())) { // GPS未开启时弹出dialog
            createDialog();
        }

        init(view);

        setlistener(); // 控件设置事件响应

        addpoint(); // 添加位置数据

        drawpoints(); // 指定的位置集合绘制泡泡

        return view;
    }

    /**
     * 初始化变量
     */
    private void init(View view) {

        mapView = (MapView) view.findViewById(R.id.bmapView);
        mapView.showZoomControls(false);

        baiduMap = mapView.getMap();

        locationbt = (ImageView) view.findViewById(R.id.locationimageview);

        zoominbt = (ImageView) view.findViewById(R.id.zoomin);
        zoomoutbt = (ImageView) view.findViewById(R.id.zoomout);

        mDisplay = ((Activity) getActivity()).getWindowManager().getDefaultDisplay();

    }

    /**
     * 控件的事件响应函数
     */
    private void setlistener() {
        // TODO Auto-generated method stub

        locationbt.setOnClickListener(new OnClickListener() { // 重新将地图的焦点移动到定位处

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    LatLng ll = new LatLng(bdlocation.getLatitude(), bdlocation.getLongitude());

                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(19);
                    baiduMap.animateMapStatus(mapStatusUpdate);

                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                    baiduMap.animateMapStatus(u);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        zoominbt.setOnClickListener(new OnClickListener() { // 地图放大按钮事件响应

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomBy(1);
                baiduMap.animateMapStatus(mapStatusUpdate);
            }
        });

        zoomoutbt.setOnClickListener(new OnClickListener() { // 地图缩小按钮事件响应

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomBy(-1);
                baiduMap.animateMapStatus(mapStatusUpdate);
            }
        });

        baiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() { // 地图状态改变事件响应

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                // TODO Auto-generated method stub
                if (arg0.zoom == 19) {
                    zoominbt.setImageResource(R.drawable.zoomin_dis);
                } else if (arg0.zoom == 3) {
                    zoomoutbt.setImageResource(R.drawable.zoomout_dis);
                } else {
                    zoominbt.setImageResource(R.drawable.zoomin);
                    zoomoutbt.setImageResource(R.drawable.zoomout);
                }
            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {
                // TODO Auto-generated method stub

            }
        });

        baiduMap.setOnMapTouchListener(new OnMapTouchListener() { // 地图触摸事件，触摸地图隐藏所有的泡泡

            @Override
            public void onTouch(MotionEvent arg0) {
                // TODO Auto-generated method stub
                baiduMap.hideInfoWindow();
            }
        });

        baiduMap.setOnMarkerClickListener(new OnMarkerClickListener() { // marker（泡泡）触摸弹出框事件

            @Override
            public boolean onMarkerClick(Marker arg0) {
                // TODO Auto-generated method stub

                TextView textView = new TextView(getActivity());
                //				textView.setBackgroundResource(R.drawable.chat);
                textView.setText(arg0.getTitle());
                // 定义用于显示该InfoWindow的坐标点
                // 创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                mInfoWindow = new InfoWindow(textView, arg0.getPosition(), -60);
                // 显示InfoWindow
                baiduMap.showInfoWindow(mInfoWindow);

                final String aString = arg0.getTitle();

                textView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Toast.makeText(getActivity(), aString, 0).show();
                    }
                });

                return true;
            }
        });

    }

    /**
     * 添加地图泡泡位置数据
     */
    private void addpoint() {
        // TODO Auto-generated method stub

        db = new MyDatabase(getActivity(), "Points").getWritableDatabase();

        Cursor cursor =
                db.query("PointsSql", new String[]{"ssid", "mac", "Lat", "Lng"}, null, null, null,
                         null, null);

        while (cursor.moveToNext()) {

            lin = new HashMap<String, Object>();
            lin.put("Title", cursor.getString(0));
            lin.put("Mac", cursor.getString(1));
            lin.put("X", Double.parseDouble(cursor.getString(2)));
            lin.put("Y", Double.parseDouble(cursor.getString(3)));
            points.add(lin);
        }

        db.close();

    }

    /**
     * 在指定位置集绘制泡泡
     */
    private void drawpoints() {
        // TODO Auto-generated method stub
        for (int i = 0; i < points.size(); i++) {
            point = new LatLng((double) points.get(i).get("X"), (double) points.get(i).get("Y"));
            drawpaopao(point, points.get(i).get("Title") + "\n" + points.get(i).get("Mac"));
        }
    }

    /**
     * 创建dialog提示用户GPS未开启，点击确定跳转到系统设置界面
     */
    private void createDialog() {

        new AlertDialog.Builder(getActivity()).setMessage("检测到GPS或移动网络未开启，同时开启可有效提高精度，确认开启吗？")
                                              .setPositiveButton("确认",
                                                                 new DialogInterface.OnClickListener() {

                                                                     @Override
                                                                     public void onClick(
                                                                             DialogInterface dialog,
                                                                             int which)
                                                                     {
                                                                         // TODO Auto-generated method stub
                                                                         Intent intent = new Intent(
                                                                                 Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                                         startActivityForResult(
                                                                                 intent,
                                                                                 0); // 此为设置完成后返回到获取界面
                                                                     }
                                                                 }).setNegativeButton("取消", null)
                                              .create().show();
    }

    /**
     * 开启传感器
     */
    @SuppressWarnings("deprecation")
    private void sensorstart() {
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            // 获得方向传感器
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        // 注册
        if (sensor != null) {// SensorManager.SENSOR_DELAY_UI
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    /**
     * 注册广播
     */
    private void regeistbroadcast() {
        // TODO Auto-generated method stub
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        getActivity().registerReceiver(mReceiver, iFilter);
    }

    /**
     * 改变地图中心点的位置
     */
    private void setcenter(LatLng point) {
        // TODO Auto-generated method stub
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(point);
        baiduMap.animateMapStatus(u);
    }

    /**
     * 在地图上画出泡泡
     */
    private void drawpaopao(LatLng point, String locationname) {
        // TODO Auto-generated method stub

        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.wifilogo);

        OverlayOptions option =
                new MarkerOptions().position(point).icon(bitmap).title(locationname);
        // 在地图上添加Marker，并显示
        baiduMap.addOverlay(option);

    }

    /**
     * 设置定位信息，获取定位位置，开启定位
     */
    private void getlocation() {
        // TODO Auto-generated method stub
        locationClient = new LocationClient(getActivity());

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(1000); // 设置发起定位请求的间隔时间为1000ms
        option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
        locationClient.setLocOption(option);

        locationListener = new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                // TODO Auto-generated method stub

                if (location == null) {
                    return;
                }

                bdlocation = location;

                if (location == null || mapView == null) {
                    return;
                }
                MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(lastX).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                baiduMap.setMyLocationData(locData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                    setcenter(ll);
                }
            }

        };

        locationClient.registerLocationListener(locationListener);

        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
        // 定位初始化
        baiduMap.setMyLocationConfigeration(
                new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true,
                                            null));

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        float x = event.values[0];

        if (Math.abs(x - lastX) > 0.01 && !isFirstLoc && bdlocation != null) {

            MyLocationData locData = new MyLocationData.Builder()
                    // 设置定位数据的精度信息，单位：米
                    .accuracy(bdlocation.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(x).latitude(bdlocation.getLatitude())
                    .longitude(bdlocation.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理

        sharedPreferences = getActivity()
                .getSharedPreferences("local", Context.MODE_PRIVATE); // 用来存储上次退出时的位置，避免刚进入软件是定位在北京

        // if (sharedPreferences.getFloat("latitude", 0) != 0) {
        //
        // LatLng aLatLng = new LatLng(sharedPreferences.getFloat("latitude",
        // 0), sharedPreferences.getFloat("longitude", 0));
        // setcenter(aLatLng);
        //
        // MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(18);
        // baiduMap.animateMapStatus(mapStatusUpdate);
        //
        // System.out.println("设置焦点成功");
        // }

        if (!getActivity().getIntent().getBooleanExtra("isFirstLocExtra", true)) { // 判断是否由其他页面打开地图
            isFirstLoc = getActivity().getIntent().getBooleanExtra("isFirstLocExtra", true);

            System.out.println("onResume执行了");

            LatLng aLatLng = new LatLng(
                    Float.parseFloat(getActivity().getIntent().getStringExtra("latitude")),
                    Float.parseFloat(getActivity().getIntent().getStringExtra("longitude")));
            setcenter(aLatLng);

            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(18);
            baiduMap.animateMapStatus(mapStatusUpdate);

            getActivity().getIntent().removeExtra("isFirstLocExtra"); // 移除intent消息
            getActivity().getIntent().removeExtra("latitude");
            getActivity().getIntent().removeExtra("longitude");

            System.out.println("获取数据成功" + aLatLng.toString());

        } else if (sharedPreferences.getFloat("latitude", 0) != 0) {

            LatLng aLatLng = new LatLng(sharedPreferences.getFloat("latitude", 0),
                                        sharedPreferences.getFloat("longitude", 0));
            setcenter(aLatLng);

            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(18);
            baiduMap.animateMapStatus(mapStatusUpdate);

            System.out.println("设置焦点成功");

        }

        mapView.onResume();

        getlocation();
        locationClient.start(); // 定位
        sensorstart();

        System.out.println("onResume");

    }

    @Override
    public void onPause() {
        super.onPause();

        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
        locationClient.stop();

        sensorManager.unregisterListener(this);

        // try {
        // Editor editor = sharedPreferences.edit();
        // editor.putFloat("latitude", (float) bdlocation.getLatitude());
        // editor.putFloat("longitude", (float) bdlocation.getLongitude());
        // editor.commit();
        // System.out.println("存储地址成功");
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        getActivity().unregisterReceiver(mReceiver);

        System.out.println("onDestroy");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.wifi_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.history_map:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().addToBackStack(null)
                               .replace(R.id.fragmentContainer, new HistoryLocationFragment())
                               .commit();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @author Administrator 地图验证key广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Toast.makeText(context, "权限错误", 0).show();
            }
        }
    }


}
