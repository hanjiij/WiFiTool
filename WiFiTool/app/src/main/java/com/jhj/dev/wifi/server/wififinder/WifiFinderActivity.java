package com.jhj.dev.wifi.server.wififinder;

import android.support.v4.app.Fragment;

import com.jhj.dev.wifi.server.BaseActivity;

public class WifiFinderActivity extends BaseActivity {

    @Override
    public Fragment getFragment()
    {
        return new WifiFinderFragment();
    }
}
