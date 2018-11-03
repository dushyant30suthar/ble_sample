package com.sayingsdevelopers.ble.interfaces;

import android.bluetooth.*;

public interface BluetoothConnectionCallbacksListener
    {
    void onDeviceConnected(BluetoothDevice bluetoothDevice);

    void onDeviceDisconnected(BluetoothDevice bluetoothDevice);

    }
