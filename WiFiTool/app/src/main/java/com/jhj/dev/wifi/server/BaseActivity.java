package com.jhj.dev.wifi.server;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * @author 江华健
 */
public abstract class BaseActivity extends FragmentActivity {
    public abstract Fragment getFragment();

    public int getFragmentContainerId()
    {
        return R.id.fragmentContainer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(getFragmentContainerId());
        if (fragment == null) {
            fragment = getFragment();
            fragmentManager.beginTransaction().add(getFragmentContainerId(), fragment).commit();
        }
    }


}
