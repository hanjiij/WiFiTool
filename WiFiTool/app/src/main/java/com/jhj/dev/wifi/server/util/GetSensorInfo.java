package com.jhj.dev.wifi.server.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author 韩吉
 */
public class GetSensorInfo implements SensorEventListener {

    Sensor acceleratSensor, orientationSensor;
    SensorManager sensorManager;
    float X_lateral = 0;
    float Y_longitudinal = 0;
    double sum_speed = 0;
    long lasttime = System.currentTimeMillis();
    long continuetime;
    double avg = 0;
    int i = 0, j = 0;
    int Y = 20;
    int count = 0;
    int tage = 0;
    float orientation = 0;
    Context context;
    OnAcceleratedListener onAcceleratedListener;
    OnOrientationListener onOrientationListener;
    private long time = System.currentTimeMillis();

    public GetSensorInfo(Context context) {
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    /**
     * 初始化传感器
     */
    @SuppressWarnings({"deprecation", "static-access"})
    public void StartSensor() {
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);

        if (sensorManager != null) {
            // 获得方向传感器
            acceleratSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        // 注册
        if (acceleratSensor != null) {// SensorManager.SENSOR_DELAY_UI
            sensorManager.registerListener(this, acceleratSensor, SensorManager.SENSOR_DELAY_GAME);
            sensorManager
                    .registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /**
     * 取消传感器
     */
    public void StopSensor() {
        sensorManager.unregisterListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) { // 加速度传感器

            avg += event.values[1];
            i++;

            if ((System.currentTimeMillis() - time) > 10) { // 变化时间间距为10毫秒
                avg = avg / i;

                Y++;

                if (avg < -0.7 &&
                    (System.currentTimeMillis() - lasttime) > 200)
                { // 10毫秒平均加速度为大于0.7且持续且高峰出现时间间隔为200毫秒

                    tage++;
                    if (tage > 4) { // 高峰持续时间超过60毫秒视为行走一步

                        ++count;
                        onAcceleratedListener.onAcceleratedChanged(count);

                        lasttime = System.currentTimeMillis();
                        tage = 0;
                    }
                }

                continuetime = System.currentTimeMillis();
                i = 0;
                time = System.currentTimeMillis();
                avg = 0;
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) { // 方向传感器

            orientation = event.values[0];

            onOrientationListener.onOrientationChanged(orientation);
        }

    }

    public void setOnAcceleratedListener(OnAcceleratedListener onAcceleratedListener) {
        this.onAcceleratedListener = onAcceleratedListener;
    }

    public void setOnOrientationListener(OnOrientationListener onOrientationListener) {
        this.onOrientationListener = onOrientationListener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    public interface OnAcceleratedListener {
        void onAcceleratedChanged(int count);
    }

    public interface OnOrientationListener {
        void onOrientationChanged(float orientation);
    }

}
