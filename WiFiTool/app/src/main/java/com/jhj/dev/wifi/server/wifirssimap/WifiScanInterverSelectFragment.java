package com.jhj.dev.wifi.server.wifirssimap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jhj.dev.wifi.server.R;


public class WifiScanInterverSelectFragment extends DialogFragment {
    /**
     * Wifi信号值改变监听器
     */
    private WifiRSSIMonitor rSSIMonitor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rSSIMonitor = WifiRSSIMonitor.getInstance(getActivity());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_scanInterverSelect_title)
                .setSingleChoiceItems(R.array.wifiScanInterverLv, rSSIMonitor.getScanInterverLv(),
                                      new OnClickListener() {

                                          @Override
                                          public void onClick(DialogInterface dialog, int which) {
                                              rSSIMonitor.setScanInterverLv(which);
                                              dialog.dismiss();
                                          }
                                      }).create();
        return dialog;
    }


}
