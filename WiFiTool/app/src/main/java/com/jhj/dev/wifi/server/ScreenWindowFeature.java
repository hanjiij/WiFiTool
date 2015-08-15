package com.jhj.dev.wifi.server;

import android.content.Context;


public class ScreenWindowFeature {
    private static ScreenWindowFeature windowFeature;
    public boolean isLFScreen;
    private Context appContext;

    public ScreenWindowFeature(Context context)
    {
        appContext = context;
    }

    public static ScreenWindowFeature getInstance(Context context)
    {
        if (windowFeature == null) {
            windowFeature = new ScreenWindowFeature(context.getApplicationContext());
        }
        return windowFeature;
    }

    public boolean isLFScreen()
    {
        return isLFScreen;
    }

    public void setLFScreen(boolean isLFScreen)
    {
        this.isLFScreen = isLFScreen;
    }


}
