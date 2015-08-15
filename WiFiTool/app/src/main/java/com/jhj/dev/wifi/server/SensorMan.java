package com.jhj.dev.wifi.server;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorMan implements SensorEventListener {
    private static SensorMan sensorMan;

    private Context appContext;

    private SensorManager sensorManager;

    private OnSensorDataChangedListener listener;


    public SensorMan(Context context)
    {
        appContext = context;
        sensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
    }

    public static SensorMan getInstance(Context context)
    {
        if (sensorMan == null) {
            sensorMan = new SensorMan(context.getApplicationContext());
        }
        return sensorMan;
    }

    public void setOnSensorDataChangedListener(OnSensorDataChangedListener listener)
    {
        this.listener = listener;
    }

    public void registerListener()
    {
        sensorManager.registerListener(sensorMan, sensorManager
                                               .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                                       SensorManager.SENSOR_DELAY_GAME, 0);
    }

    public void unregisterListener()
    {
        sensorManager.unregisterListener(sensorMan);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        float[] values = event.values;

        //		if (Math.abs(values[1])>0.6)
        //		{
        //			System.out.println(
        //					" ,values 1--->  "+values[1]
        //							);
        //		}


        listener.onSensorDataChanged(values[1]);


        //		currentAcc=values[1];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // TODO Auto-generated method stub

    }

    public interface OnSensorDataChangedListener {
        void onSensorDataChanged(float newData);
    }


}
