package com.jhj.dev.wifi.server.wifirssimap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.view.WindowManager;

import com.jhj.dev.wifi.server.BaseActivity;
import com.jhj.dev.wifi.server.DialogMgr;

/**
 * @author 江华健
 */
public class WifiRSSIMapActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DialogMgr.changeActivity(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment getFragment()
    {
        return new WifiRSSIMapFragment();
    }

}
