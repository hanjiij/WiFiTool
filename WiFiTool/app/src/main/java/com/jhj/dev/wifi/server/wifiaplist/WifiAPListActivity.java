package com.jhj.dev.wifi.server.wifiaplist;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.jhj.dev.wifi.server.BaseActivity;


public class WifiAPListActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment getFragment()
    {
        return new WifiAPListFragment();
    }

}
