package com.jhj.dev.wifi.server;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.jhj.dev.wifi.server.wifiaplist.WifiAPConnectFragment;
import com.jhj.dev.wifi.server.wifiaplist.WifiAPDetailsFragment;
import com.jhj.dev.wifi.server.wififinder.SelectAPFragment;
import com.jhj.dev.wifi.server.wifirssimap.WifiFilteredFragment;
import com.jhj.dev.wifi.server.wifirssimap.WifiScanInterverSelectFragment;

import java.util.LinkedHashSet;

public class DialogMgr {
    private static final String TAG_WIFIDISABLEDHINTDIALOG = "WifiDisabledHintDialog";

    private static final String TAG_WIFIFILTEREDDIALOG = "WifiFilteredDialog";

    private static final String TAG_WIFISCANINTERVERSELECTDIALOG = "WifiScanInterverSelectDialog";

    private static final String TAG_SELECTAPFRAGMENTDIALOG = "selectAPFragmentDialog";

    private static final String TAG_WIFIAPDETAILSDIALOG = "wifiAPDetailsDialog";

    private static final String TAG_WIFIAPCONNECTDIALOG = "wifiAPConnectDialog";
    private static final String TAG_ABOUTDIALOG = "aboutDialog";

    private static final String TAG_HELPDIALOG = "helpDialog";
    private static DialogMgr dialogMgr;
    private static FragmentActivity activity;
    private static FragmentManager fragmentManager;
    private Context appContext;

    public DialogMgr(Context context, FragmentActivity originActivity) {
        appContext = context;
        activity = originActivity;
        fragmentManager = activity.getSupportFragmentManager();
    }

    public static DialogMgr getInstance(Context context, FragmentActivity originActivity) {
        if (dialogMgr == null) {
            dialogMgr = new DialogMgr(context.getApplicationContext(), originActivity);
        }
        return dialogMgr;
    }


    public static void changeActivity(FragmentActivity otherActivity) {
        activity = otherActivity;
        fragmentManager = activity.getSupportFragmentManager();
    }

    public void showWifiDisabledHintDialog() {
        DialogFragment wifiDisabledHintDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_WIFIDISABLEDHINTDIALOG);
        if (wifiDisabledHintDialog == null) {
            wifiDisabledHintDialog = new WifiDisabledHintFragment();
        }
        wifiDisabledHintDialog.show(fragmentManager, TAG_WIFIDISABLEDHINTDIALOG);
    }

    public void dismissWifiDisabledHintDialog() {
        DialogFragment wifiDisabledHintDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_WIFIDISABLEDHINTDIALOG);
        if (wifiDisabledHintDialog != null) {
            wifiDisabledHintDialog.getDialog().dismiss();
        }
    }

    public void showWifiAPDetailsDialog(String apName, String apDetails) {
        DialogFragment wifiAPDetailsDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_WIFIAPDETAILSDIALOG);
        if (wifiAPDetailsDialog == null) {
            wifiAPDetailsDialog = WifiAPDetailsFragment.getInstance(apName, apDetails);
        }
        wifiAPDetailsDialog.show(fragmentManager, TAG_WIFIAPDETAILSDIALOG);
    }

    public void showWifiAPConnectDialog(String SSID) {
        DialogFragment wifiAPConnectDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_WIFIAPCONNECTDIALOG);
        if (wifiAPConnectDialog == null) {
            wifiAPConnectDialog = WifiAPConnectFragment.getInstance(SSID);
        }
        wifiAPConnectDialog.show(fragmentManager, TAG_WIFIAPCONNECTDIALOG);
    }

    public void showWifiFilteredDialog(CharSequence[] apInfos, boolean[] checkedItems,
                                       LinkedHashSet<String> beforeFilteredAPBSSIDs,
                                       LinkedHashSet<String> preFilteredAPBSSIDs)
    {
        DialogFragment wifiFilteredDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_WIFIFILTEREDDIALOG);
        if (wifiFilteredDialog == null) {
            wifiFilteredDialog = WifiFilteredFragment
                    .getInstance(apInfos, checkedItems, beforeFilteredAPBSSIDs,
                                 preFilteredAPBSSIDs);
        }
        wifiFilteredDialog.show(fragmentManager, TAG_WIFIFILTEREDDIALOG);
    }

    public void showScanInterverSelectDialog() {
        DialogFragment scanInterverSelectDialog = (DialogFragment) fragmentManager
                .findFragmentByTag(TAG_WIFISCANINTERVERSELECTDIALOG);

        if (scanInterverSelectDialog == null) {
            scanInterverSelectDialog = new WifiScanInterverSelectFragment();
        }
        scanInterverSelectDialog.show(fragmentManager, TAG_WIFISCANINTERVERSELECTDIALOG);
    }

    public void showSelectAPDialog(Fragment tagFragment, int requestCode, String[] apInfos,
                                   String beforeSpecAPBSSID)
    {
        DialogFragment selectAPDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_SELECTAPFRAGMENTDIALOG);
        if (selectAPDialog == null) {
            selectAPDialog = SelectAPFragment.getInstance(apInfos, beforeSpecAPBSSID);
            selectAPDialog.setTargetFragment(tagFragment, requestCode);
        }

        selectAPDialog.show(fragmentManager, TAG_SELECTAPFRAGMENTDIALOG);
    }

    public void showAboutDialog()
    {
        DialogFragment aboutDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_ABOUTDIALOG);
        if (aboutDialog == null) {
            aboutDialog = new AboutFragment();
        }
        aboutDialog.show(fragmentManager, TAG_ABOUTDIALOG);
    }

    public void showHelpDialog()
    {
        DialogFragment helpDialog =
                (DialogFragment) fragmentManager.findFragmentByTag(TAG_HELPDIALOG);
        if (helpDialog == null) {
            helpDialog = new HelpFragment();
        }
        helpDialog.show(fragmentManager, TAG_HELPDIALOG);
    }
}
