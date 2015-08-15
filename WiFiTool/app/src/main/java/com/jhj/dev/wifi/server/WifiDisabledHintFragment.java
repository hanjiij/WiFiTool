package com.jhj.dev.wifi.server;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

/**
 * @author 江华健
 */
public class WifiDisabledHintFragment extends DialogFragment {
    /**
     * Wifi管理员
     */
    private WifiMan wifiMan;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        wifiMan = WifiMan.getInstance(getActivity());

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.title_Wifi)
                                                     .setMessage(R.string.msg_Wifi)
                                                     .setPositiveButton(
                                                             R.string.openWifi,
                                                             new OnClickListener() {

                                                                 @Override
                                                                 public void onClick(
                                                                         DialogInterface dialog,
                                                                         int which)
                                                                 {
                                                                     wifiMan.openWifi();

                                                                 }
                                                             }).setNeutralButton(
                        R.string.setting_Wifi, new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(intent);
                            }
                        }).setNegativeButton(R.string.dismiss_Wifi,
                                             new OnClickListener() {

                                                 @Override
                                                 public void onClick(DialogInterface dialog,
                                                                     int which)
                                                 {
                                                     getActivity().finish();
                                                 }
                                             }).setCancelable(false).create();

    }


}
