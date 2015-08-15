package com.jhj.dev.wifi.server.wififinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.jhj.dev.wifi.server.R;


public class SelectAPFragment extends DialogFragment {
    public static final String EXTRA_APNAME = "apName";

    private static final String APINFOS = "apInfos";

    private static final String BEFORESPECAPBSSID = "beforeSpecAPBSSID";

    /**
     * 之前指定的接入点MAC地址
     */
    private String beforeSpecAPBSSID;

    /**
     * 所有接入点信息
     */
    private String[] apInfos;

    public static SelectAPFragment getInstance(String[] apInfos, String beforeSpecAPBSSID) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(APINFOS, apInfos);
        bundle.putString(BEFORESPECAPBSSID, beforeSpecAPBSSID);
        SelectAPFragment selectAPFragment = new SelectAPFragment();
        selectAPFragment.setArguments(bundle);
        return selectAPFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        apInfos = bundle.getStringArray(APINFOS);
        beforeSpecAPBSSID = bundle.getString(BEFORESPECAPBSSID);
    }

    /**
     * 发送接入点名字给目标Fragment
     *
     * @param apInfo     所有接入点信息
     * @param resultCode 结果码
     */
    private void sendResult(String apInfo, int resultCode) {
        Fragment tagFragment = getTargetFragment();
        if (tagFragment == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_APNAME, apInfo);
        tagFragment.onActivityResult(WifiFinderFragment.REQUEST_APNAME, resultCode, intent);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =
                new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_selectAP_title)
                                                      .setSingleChoiceItems(apInfos,
                                                                            findCheckedItem(),
                                                                            new OnClickListener() {
                                                                                @Override
                                                                                public void onClick(
                                                                                        DialogInterface dialog,
                                                                                        int which)
                                                                                {
                                                                                    sendResult(
                                                                                            apInfos[which],
                                                                                            Activity.RESULT_OK);
                                                                                    dialog.dismiss();
                                                                                }
                                                                            }).create();
        return dialog;
    }


    /**
     * 寻找之前指定的接入点MAC地址在接入点列表信息里的索引
     *
     * @return 之前指定的接入点MAC地址在接入点列表信息里的索引
     */
    private int findCheckedItem() {
        for (int i = 0; i < apInfos.length; i++) {
            String apInfo = apInfos[i];
            if (apInfo.split("\n")[1].equals(beforeSpecAPBSSID)) {
                return i;
            }
        }
        return -1;
    }

}
