package com.sayingsdevelopers.ble.managers;

import android.bluetooth.BluetoothDevice;

import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;

import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.sayingsdevelopers.ble.EntryPoint.rxBleClient;

abstract public class BluetoothConnectionManager
        implements
        Observer<RxBleConnection>
    {
    Single<RxBleDeviceServices> serviceList;
    RxBleConnection connection;
    private RxBleDevice device;
    private String address;
    private Disposable disposable;

    public BluetoothConnectionManager(
            String address)
        {
        this.address = address;
        }

    public void connectDevice()
        {
        device = rxBleClient.getBleDevice(address);
        device.establishConnection(false).observeOn(AndroidSchedulers.mainThread()).subscribe(this);
        }

    public void disconnectDevice()
        {
        disposable.dispose();
        }

    abstract public void onBluetoothDeviceConnected(
            BluetoothDevice bluetoothDevice);

    abstract public void onBluetoothDeviceDisconnected(
            BluetoothDevice bluetoothDevice);

    @Override
    public void onSubscribe(
            Disposable d)
        {
        disposable = d;
        }

    @Override
    public void onNext(
            RxBleConnection rxBleConnection)
        {
        onBluetoothDeviceConnected(device.getBluetoothDevice());
        }


    @Override
    public void onError(
            Throwable e)
        {
        onBluetoothDeviceDisconnected(device.getBluetoothDevice());
        }

    @Override
    public void onComplete()
        {

        }

    }
