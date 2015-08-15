package com.jhj.dev.wifi.server.wifiaplist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.WifiMan;

/**
 * @author 江华健
 */
public class WifiAPConnectFragment extends DialogFragment {
    private static final String APSSID = "SSID";
    private String SSID;

    private WifiMan wifiMan;

    public static WifiAPConnectFragment getInstance(String SSID) {
        Bundle bundle = new Bundle();
        bundle.putString(APSSID, SSID);
        WifiAPConnectFragment apConnectFragment = new WifiAPConnectFragment();
        apConnectFragment.setArguments(bundle);
        return apConnectFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        SSID = bundle.getString(SSID);
        wifiMan = WifiMan.getInstance(getActivity());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView =
                getActivity().getLayoutInflater().inflate(R.layout.dialog_connect_ap, null);


        final EditText et_wifiPwd = (EditText) dialogView.findViewById(R.id.et_wifiPwd);

        AlertDialog dialog =
                new AlertDialog.Builder(getActivity()).setTitle(SSID).setView(dialogView)
                                                      .setNegativeButton("取消", null)
                                                      .setPositiveButton("连接",
                                                                         new OnClickListener() {
                                                                             @Override
                                                                             public void onClick(
                                                                                     DialogInterface dialog,
                                                                                     int which)
                                                                             {
                                                                                 wifiMan.connectWifi(
                                                                                         SSID,
                                                                                         et_wifiPwd
                                                                                                 .getText()
                                                                                                 .toString());
                                                                             }
                                                                         }).create();

        dialog.setOnShowListener(new OnShowListener() {
            Button but_connect = null;

            @Override
            public void onShow(DialogInterface dialog) {
                but_connect = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                but_connect.setEnabled(false);
                et_wifiPwd.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        but_connect.setEnabled(et_wifiPwd.getText().toString().length() > 7);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });
        //		final Button but_connect = dialog
        //				.getButton(DialogInterface.BUTTON_POSITIVE);
        //		but_connect.setEnabled(false);


        return dialog;

    }


}
