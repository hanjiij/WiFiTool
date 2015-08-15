package com.jhj.dev.wifi.server.fragment;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jhj.dev.wifi.server.WifiLocation;

/**
 * @author 韩吉
 */
@SuppressWarnings("deprecation")
public class WifilocationFragment extends Fragment {

    LocalActivityManager manager;
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        manager = new LocalActivityManager(getActivity(), true);
        manager.dispatchCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub

        if (view == null) {
            Intent intent = new Intent(getActivity(), WifiLocation.class);
            view = manager.startActivity("WifiLocation", intent).getDecorView();
        } else {
            ((FrameLayout) view.getParent()).removeView(view);
        }

        return view;
    }
}
