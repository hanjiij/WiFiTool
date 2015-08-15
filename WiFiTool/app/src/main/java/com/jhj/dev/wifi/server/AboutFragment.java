package com.jhj.dev.wifi.server;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * @author 江华健
 */
public class AboutFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View aboutView = getActivity().getLayoutInflater()
                                      .inflate(R.layout.dialog_about, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.action_about).setView(aboutView)
                .setPositiveButton(R.string.dialog_txt_ok, null).create();

    }


}
