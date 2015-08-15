package com.jhj.dev.wifi.server.wifiaplist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jhj.dev.wifi.server.R;


public class WifiAPDetailsFragment extends DialogFragment {
    private static final String APNAME = "apName";
    private static final String APDETAILS = "apDetails";
    private String apName;
    private String apDetails;


    public static WifiAPDetailsFragment getInstance(String apName, String apDetails) {
        Bundle bundle = new Bundle();
        bundle.putString(APNAME, apName);
        bundle.putString(APDETAILS, apDetails);
        WifiAPDetailsFragment apDetailsFragment = new WifiAPDetailsFragment();
        apDetailsFragment.setArguments(bundle);
        return apDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        this.apName = bundle.getString(APNAME);
        this.apDetails = bundle.getString(APDETAILS);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity()).setTitle(apName).setMessage(apDetails)
                                                     .setNegativeButton(R.string.dialog_cancle,
                                                                        null).create();

    }


}
