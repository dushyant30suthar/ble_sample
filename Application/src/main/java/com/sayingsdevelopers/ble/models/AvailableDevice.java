package com.sayingsdevelopers.ble.models;

import android.bluetooth.*;

public class AvailableDevice
    {
    private BluetoothDevice bluetoothDevice;
    private long availabilityRecordedAt;

    public AvailableDevice(BluetoothDevice bluetoothDevice, long availabilityRecordedAt)
        {
        this.bluetoothDevice = bluetoothDevice;
        this.availabilityRecordedAt = availabilityRecordedAt;
        }

    public BluetoothDevice getBluetoothDevice()
        {
        return bluetoothDevice;
        }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice)
        {
        this.bluetoothDevice = bluetoothDevice;
        }

    public long getAvailabilityRecordedAt()
        {
        return availabilityRecordedAt;
        }

    public void setAvailabilityRecordedAt(long availabilityRecordedAt)
        {
        this.availabilityRecordedAt = availabilityRecordedAt;
        }

    @Override
    public String toString()
        {
        return bluetoothDevice.getAddress();
        }
    }
