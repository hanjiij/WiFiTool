package com.jhj.dev.wifi.server.util;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * @author 韩吉
 */
public class GetLocation {

    private LocationClient locationClient;
    private BDLocation bdLocation;
    private BDLocationListener bdLocationListener;
    private Context context;
    private OnLocationListener onLocationListener;

    public GetLocation(Context context) {
        // TODO Auto-generated constructor stub
        this.context = context;
        initLocation();
    }

    /**
     * 初始化使用变量，和定位信息
     */
    private void initLocation() {
        locationClient = new LocationClient(context);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(1000); // 设置发起定位请求的间隔时间为1000ms
        option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
        locationClient.setLocOption(option);

        bdLocationListener = new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                // TODO Auto-generated method stub

                onLocationListener.onLocationChanged(location);
                bdLocation = location;
            }

        };

        locationClient.registerLocationListener(bdLocationListener);
    }

    public void setOnLocationListener(OnLocationListener onLocationListener) {
        this.onLocationListener = onLocationListener;
    }

    /**
     * 开启GPS
     */
    public void StartLocation() {
        locationClient.start();
    }

    /**
     * 关闭GPS
     */
    public void StopLocation() {
        locationClient.stop();
    }

    /**
     * 获取位置信息
     *
     * @return 定位到的坐标
     */
    public BDLocation getBdLocation() {
        return bdLocation;
    }

    public interface OnLocationListener {
        void onLocationChanged(BDLocation bdLocation);
    }
}
