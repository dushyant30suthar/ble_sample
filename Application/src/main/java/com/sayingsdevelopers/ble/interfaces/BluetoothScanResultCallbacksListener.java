package com.sayingsdevelopers.ble.interfaces;

import android.bluetooth.BluetoothDevice;

public interface BluetoothScanResultCallbacksListener
    {
    public void onDeviceAvailable(BluetoothDevice bluetoothDevice);
    public void onDeviceUnAvailable(BluetoothDevice bluetoothDevice);
    }
