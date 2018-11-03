package com.sayingsdevelopers.ble.backgroundcomponents;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sayingsdevelopers.ble.EntryPoint;
import com.sayingsdevelopers.ble.interfaces.BluetoothConnectionCallbacksListener;
import com.sayingsdevelopers.ble.interfaces.BluetoothScanResultCallbacksListener;
import com.sayingsdevelopers.ble.interfaces.ChecksPerformedListener;
import com.sayingsdevelopers.ble.managers.BluetoothConnectionManager;
import com.sayingsdevelopers.ble.managers.BluetoothScanResultManager;
import com.sayingsdevelopers.ble.models.SavedDevice;
import com.sayingsdevelopers.ble.models.SavedDevice_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import io.objectbox.Box;

public class ConnectionService
        extends
        Service
    {
    private static String TAG = ConnectionService.class.getSimpleName();
    Binder binder;
    Box<SavedDevice> savedDeviceBox;
    Handler handler;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    List<BluetoothScanResultCallbacksListener> bluetoothScanResultCallbackListenerList;
    List<BluetoothConnectionCallbacksListener> bluetoothConnectionCallbacksListenerList;
    Map<String, BluetoothConnectionManager> bluetoothConnectionManagerMap;
    BluetoothScanResultManager bluetoothScanResultManager;

    public void addBluetoothConnectionCallbackListener(
            BluetoothConnectionCallbacksListener bluetoothConnectionCallbacksListener)
        {
        if (!bluetoothConnectionCallbacksListenerList.contains(bluetoothConnectionCallbacksListener))
            {
            bluetoothConnectionCallbacksListenerList.add(bluetoothConnectionCallbacksListener);
            }
        }

    public void disconnectDevice(
            String address)
        {
        if (getConnectionManager(address) != null)
            {
            getConnectionManager(address).disconnectDevice();
            }
        }

    public void everyThingIsChecked(
            ChecksPerformedListener checksPerformedListener,
            boolean passed)
        {
        checksPerformedListener.onEverythingIsChecked(passed);
        }

    public BluetoothAdapter getBluetoothAdapter()
        {
        return bluetoothAdapter;
        }

    public BluetoothConnectionManager getConnectionManager(
            String address)
        {
        return bluetoothConnectionManagerMap.get(address);
        }

    public boolean isDeviceConnected(
            String address)
        {
        return bluetoothConnectionManagerMap.containsKey(address);
        }

    public void addBluetoothScanCallbacksListener(
            BluetoothScanResultCallbacksListener bluetoothScanResultCallbacksListener)
        {
        if (!this.bluetoothScanResultCallbackListenerList.contains(bluetoothScanResultCallbacksListener))
            {
            this.bluetoothScanResultCallbackListenerList.add(bluetoothScanResultCallbacksListener);
            }
        }

    @Override
    public void onCreate()
        {
        super.onCreate();
        handler = new Handler();
        binder = new LocalBinder();
        bluetoothScanResultCallbackListenerList = new ArrayList<>();
        bluetoothConnectionCallbacksListenerList = new ArrayList<>();
        bluetoothConnectionManagerMap = new HashMap<>();
        savedDeviceBox = ((EntryPoint) getApplication()).getBoxStore().boxFor(SavedDevice.class);
        }

    public void connectDevice(
            String address)
        {
        bluetoothConnectionManagerMap.put(address, new BluetoothConnectionManager(address)
            {
            @Override
            public void onBluetoothDeviceConnected(
                    BluetoothDevice bluetoothDevice)
                {
                for (BluetoothConnectionCallbacksListener bluetoothConnectionCallbacksListenerListener : bluetoothConnectionCallbacksListenerList)
                    {
                    bluetoothConnectionCallbacksListenerListener.onDeviceConnected(bluetoothDevice);
                    }
                SavedDevice savedDevice = savedDeviceBox.query().equal(SavedDevice_.address, address).build().findFirst();
                if (savedDevice != null)
                    {
                    savedDevice.setConnectionStatus("Connected");
                    savedDeviceBox.put(savedDevice);
                    }
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                }

            @Override
            public void onBluetoothDeviceDisconnected(
                    BluetoothDevice bluetoothDevice)
                {
                for (BluetoothConnectionCallbacksListener bluetoothConnectionCallbacksListenerListener : bluetoothConnectionCallbacksListenerList)
                    {
                    bluetoothConnectionCallbacksListenerListener.onDeviceDisconnected(bluetoothDevice);
                    }
                bluetoothConnectionManagerMap.remove(address);
                SavedDevice savedDevice = savedDeviceBox.query().equal(SavedDevice_.address, address).build().findFirst();
                if (savedDevice != null)
                    {
                    savedDevice.setConnectionStatus("Disconnected");
                    savedDeviceBox.put(savedDevice);
                    }
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                }
            });
        getConnectionManager(address).connectDevice();
        }

    @Override
    public void onDestroy()
        {
        for (String s : bluetoothConnectionManagerMap.keySet())
            {
            disconnectDevice(s);
            }
        for (SavedDevice savedDevice : savedDeviceBox.getAll())
            {
            savedDevice.setConnectionStatus("Disconnected");
            savedDeviceBox.put(savedDevice);
            }
        if (bluetoothScanResultManager != null && this.bluetoothScanResultCallbackListenerList.size() == 0 && bluetoothScanResultManager.getDisposable() != null)
            {
            bluetoothScanResultManager.getDisposable().dispose();
            }
        super.onDestroy();
        }

    @Nullable
    @Override
    public IBinder onBind(
            Intent intent)
        {
        return binder;
        }

    @Override
    public boolean onUnbind(
            Intent intent)
        {
        return super.onUnbind(intent);
        }

    @Override
    public void onRebind(
            Intent intent)
        {
        super.onRebind(intent);
        }

    public boolean isDeviceSupported()
        {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            {
            return false;
            }
        try
            {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            } catch (NullPointerException nullPointerException)
            {
            Log.e(TAG, "Can not get reference to Bluetooth Adapter");
            return false;
            }
        return true;
        }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId)
        {
        super.onStartCommand(intent, flags, startId);
        if (bluetoothScanResultManager == null)
            {
            bluetoothScanResultManager = new BluetoothScanResultManager()
                {
                @Override
                public void onBluetoothDeviceAvailable(
                        BluetoothDevice bluetoothDevice)
                    {
                    for (BluetoothScanResultCallbacksListener bluetoothScanResultCallbacksListener : bluetoothScanResultCallbackListenerList)
                        {
                        bluetoothScanResultCallbacksListener.onDeviceAvailable(bluetoothDevice);
                        }
                    }
                };
            bluetoothScanResultManager.startBluetoothScanning();
            }
        return START_STICKY;
        }

    public List<BluetoothDevice> getBluetoothDevicesInRange()
        {
        return bluetoothScanResultManager.getDevicesInRange();
        }

    public void removeBluetoothScanResultsCallbacksListener(
            BluetoothScanResultCallbacksListener bluetoothScanResultCallbacksListener)
        {

        this.bluetoothScanResultCallbackListenerList.remove(bluetoothScanResultCallbacksListener);
        }

    public void removeConnectionCallbackListener(
            BluetoothConnectionCallbacksListener bluetoothConnectionCallbacksListener)
        {
        bluetoothConnectionCallbacksListenerList.remove(bluetoothConnectionCallbacksListener);
        }


    public class LocalBinder
            extends
            Binder
        {
        public ConnectionService getService()
            {
            return ConnectionService.this;
            }
        }
    }
