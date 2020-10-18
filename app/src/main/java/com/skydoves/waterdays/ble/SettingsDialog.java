package com.skydoves.waterdays.ble;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.skydoves.waterdays.R;

import java.util.Objects;

public class SettingsDialog extends AppCompatDialogFragment {
//    private DeviceControlActivity mDeviceControlActivity;
    private EditText massage_et;
    //test

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder settingsDialog = new AlertDialog.Builder(
                Objects.requireNonNull(getActivity()));

        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View view = inflater.inflate(R.layout.layout_dialog, null);
//        if (getActivity() != null) {mDeviceControlActivity = (DeviceControlActivity) getActivity();}

//        settingsDialog.setView(view);
//        settingsDialog.setTitle(R.string.title_dealog);
//        settingsDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String massage = massage_et.getText().toString();
//                byte[] massage2 = {0x00, 0x01, 0x03, 0x02};
//                mDeviceControlActivity.sendMyBuffer(massage2,massage);
//            }
//        });

//        settingsDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });
//
//        massage_et = view.findViewById(R.id.massage_et);

        return settingsDialog.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
            "must implement SettingsDialogListener");
        }
    }

}
