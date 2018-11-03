package com.sayingsdevelopers.ble.managers;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.sayingsdevelopers.ble.EntryPoint.rxBleClient;

abstract public class BluetoothScanResultManager
        implements
        Observer<ScanResult>
    {
    private Disposable disposable;
    private ScanSettings scanSettingsForAvailability;
    private List<BluetoothDevice> devicesInRange;

    @Nullable
    public Disposable getDisposable()
        {
        return disposable;
        }

    abstract public void onBluetoothDeviceAvailable(
            BluetoothDevice bluetoothDevice);

    @Override
    public void onSubscribe(
            Disposable disposable)
        {
        this.disposable = disposable;
        this.devicesInRange = new ArrayList<>();
        }

    public List<BluetoothDevice> getDevicesInRange()
        {
        return devicesInRange;
        }

    @Override
    public void onError(
            Throwable e)
        {

        }

    @Override
    public void onComplete()
        {

        }

    private void setUpScanSettings()
        {
        ScanFilter[] scanFilterList = new ScanFilter[1];
        ScanFilter scanFilter = new ScanFilter.Builder().build();
        scanFilterList[0] = scanFilter;
        scanSettingsForAvailability = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        }

    public void startBluetoothScanning()
        {
        setUpScanSettings();
        rxBleClient.scanBleDevices(scanSettingsForAvailability).observeOn(AndroidSchedulers.mainThread()).subscribe(this);
        }

    @Override
    public void onNext(
            ScanResult scanResult)
        {
        if (!devicesInRange.contains(scanResult.getBleDevice().getBluetoothDevice()))
            {
            devicesInRange.add(scanResult.getBleDevice().getBluetoothDevice());
            onBluetoothDeviceAvailable(scanResult.getBleDevice().getBluetoothDevice());
            }
        }

    void stopBluetoothScanning()
        {

        }

    }
