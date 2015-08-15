package com.jhj.dev.wifi.server.wifirssimap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.model.WifiMapData;

import java.util.LinkedHashSet;
import java.util.Set;

public class WifiFilteredFragment extends DialogFragment {

    private static final String APINFOS = "apInfos";
    private static final String CHECKEDITEMS = "checkedItems";
    private static final String BEFOREFILTEREDAPBSSIDS = "beforeFilteredAPBSSIDs";
    private static final String PREFILTEREDAPBSSIDS = "preFilteredAPBSSIDs";
    private CharSequence[] apInfos;

    private boolean[] checkedItems;

    private Set<String> beforeFilteredAPBSSIDs;

    private LinkedHashSet<String> preFilteredAPBSSIDs;

    private WifiMapData mapData;

    public static WifiFilteredFragment getInstance(CharSequence[] apInfos, boolean[] checkedItems,
                                                   LinkedHashSet<String> beforeFilteredAPBSSIDs,
                                                   LinkedHashSet<String> preFilteredAPBSSIDs)
    {
        Bundle bundle = new Bundle();
        bundle.putCharSequenceArray(APINFOS, apInfos);
        bundle.putBooleanArray(CHECKEDITEMS, checkedItems);
        bundle.putSerializable(BEFOREFILTEREDAPBSSIDS, beforeFilteredAPBSSIDs);
        bundle.putSerializable(PREFILTEREDAPBSSIDS, preFilteredAPBSSIDs);
        WifiFilteredFragment filteredFragment = new WifiFilteredFragment();
        filteredFragment.setArguments(bundle);
        return filteredFragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        apInfos = bundle.getCharSequenceArray(APINFOS);
        checkedItems = bundle.getBooleanArray(CHECKEDITEMS);
        beforeFilteredAPBSSIDs =
                (LinkedHashSet<String>) bundle.getSerializable(BEFOREFILTEREDAPBSSIDS);
        preFilteredAPBSSIDs = (LinkedHashSet<String>) bundle.getSerializable(PREFILTEREDAPBSSIDS);
        mapData = WifiMapData.getInstance(getActivity());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_filteredWifi_title)
                                                     .setMultiChoiceItems(apInfos, checkedItems,
                                                                          new OnMultiChoiceClickListener() {

                                                                              @Override
                                                                              public void onClick(
                                                                                      DialogInterface dialog,
                                                                                      int which,
                                                                                      boolean isChecked)
                                                                              {
                                                                                  String
                                                                                          addedAPBSSID =
                                                                                          ((String) apInfos[which])
                                                                                                  .split("\n")[1];

                                                                                  if (isChecked) {
                                                                                      preFilteredAPBSSIDs
                                                                                              .add(addedAPBSSID);
                                                                                  } else {
                                                                                      preFilteredAPBSSIDs
                                                                                              .remove(addedAPBSSID);
                                                                                  }

                                                                              }
                                                                          }).setPositiveButton(
                        R.string.dialog_filteredWifi_posBut_text, new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                beforeFilteredAPBSSIDs.clear();
                                beforeFilteredAPBSSIDs.addAll(preFilteredAPBSSIDs);
                                mapData.setFilter(true);

                            }
                        }).setNegativeButton(R.string.dialog_cancle, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preFilteredAPBSSIDs.clear();
                    }
                }).create();
    }


}
