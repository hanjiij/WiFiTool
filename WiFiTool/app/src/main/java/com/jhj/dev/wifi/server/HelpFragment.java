package com.jhj.dev.wifi.server;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class HelpFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.action_help)
                .setMessage(R.string.dialog_txt_help)
                .setPositiveButton(R.string.dialog_txt_ok, null).create();
    }


}
