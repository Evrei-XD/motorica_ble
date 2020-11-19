package com.skydoves.waterdays.ui.activities.intro;

import android.bluetooth.BluetoothDevice;

import java.util.List;

public interface ScanView {
    void addDeviceToScanList(String item, BluetoothDevice device);
    void clearScanList();
}
