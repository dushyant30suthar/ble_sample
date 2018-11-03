package com.sayingsdevelopers.ble.foregroundcomponents.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sayingsdevelopers.ble.EntryPoint;
import com.sayingsdevelopers.ble.R;
import com.sayingsdevelopers.ble.backgroundcomponents.ConnectionService;
import com.sayingsdevelopers.ble.interfaces.BluetoothScanResultCallbacksListener;
import com.sayingsdevelopers.ble.interfaces.ChecksPerformedListener;
import com.sayingsdevelopers.ble.models.SavedDevice;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.objectbox.Box;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class ScanDevicesActivity
        extends
        AppCompatActivity
        implements
        ChecksPerformedListener,
        BluetoothScanResultCallbacksListener
    {
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_ACCESS_LOCATION = 2;
    Box<SavedDevice> savedDeviceBox;
    private ConnectionService connectionService;
    private ScannedDevicesListRecyclerViewAdapter scannedDevicesListRecyclerViewAdapter;
    private RecyclerView scannedDevicesListRecyclerView;

    private ServiceConnection serviceConnection = new ServiceConnection()
        {
        @Override
        public void onServiceConnected(
                ComponentName name,
                IBinder service)
            {
            connectionService = ((ConnectionService.LocalBinder) service).getService();
            if (!connectionService.isDeviceSupported())
                {
                Toast.makeText(getApplicationContext(), "Device not Supported", Toast.LENGTH_LONG).show();
                }
            else
                {
                setUpPermissionsAndConfigurations();
                }
            }

        @Override
        public void onServiceDisconnected(
                ComponentName name)
            {
            connectionService = null;
            }
        };

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
        {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED)
            {
            finish();
            return;
            }
        connectionService.everyThingIsChecked(ScanDevicesActivity.this, true);
        super.onActivityResult(requestCode, resultCode, data);
        }

    @Override
    protected void onPause()
        {
        super.onPause();
        if (connectionService != null)
            {
            connectionService.removeBluetoothScanResultsCallbacksListener(this);
            }
        scannedDevicesListRecyclerViewAdapter.clearDeviceList();
        }

    @Override
    protected void onResume()
        {
        super.onResume();
        Intent intent = new Intent(ScanDevicesActivity.this, ConnectionService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_IMPORTANT);
        }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults)
        {
        switch (requestCode)
            {
            case PERMISSION_ACCESS_LOCATION:
            {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                setUpBluetoothAdapter();
                }
            else
                {
                Toast.makeText(ScanDevicesActivity.this, "Sorry! App wouldn't work for you", Toast.LENGTH_LONG).show();
                }
            }
            }
        }

    @Override
    public void onCreate(
            Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_devices);
        setUpViewIds();
        setUpInitialThings();
        }

    @Override
    protected void onStop()
        {
        super.onStop();
        unbindService(serviceConnection);
        }

    @Override
    public void onDeviceAvailable(
            BluetoothDevice bluetoothDevice)
        {
        Log.d(bluetoothDevice.getName(), "isAvailable : " + true);
        scannedDevicesListRecyclerViewAdapter.addBluetoothDevice(bluetoothDevice);
        }

    @Override
    public void onDeviceUnAvailable(
            BluetoothDevice bluetoothDevice)
        {
        Log.d(bluetoothDevice.getName(), "isAvailable : " + false);
        scannedDevicesListRecyclerViewAdapter.removeBluetoothDevice(bluetoothDevice);
        }

    @Override
    public void onEverythingIsChecked(
            boolean passed)
        {
        if (passed)
            {
            connectionService.addBluetoothScanCallbacksListener(this);
            scannedDevicesListRecyclerViewAdapter.setBluetoothDeviceList(connectionService.getBluetoothDevicesInRange());
            }
        }

    private void setUpBluetoothAdapter()
        {
        BluetoothAdapter bluetoothAdapter = connectionService.getBluetoothAdapter();
        if (!bluetoothAdapter.isEnabled())
            {
            if (!bluetoothAdapter.isEnabled())
                {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
                }
            else
                {
                connectionService.everyThingIsChecked(this, true);
                }
            }
        else
            {
            connectionService.everyThingIsChecked(this, true);
            }
        }

    private void setUpInitialThings()
        {
        scannedDevicesListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scannedDevicesListRecyclerViewAdapter = new ScannedDevicesListRecyclerViewAdapter(this)
            {
            @Override
            void onDeviceSelected(
                    BluetoothDevice bluetoothDevice)
                {
                SavedDevice savedDevice = new SavedDevice();
                savedDevice.setAddress(bluetoothDevice.getAddress());
                savedDevice.setDeviceName(bluetoothDevice.getName());
                savedDevice.setId(0);
                savedDevice.setConnectionStatus("Disconnected");
                boolean isAlreadyAdded = false;
                for (SavedDevice savedDevice1 : savedDeviceBox.getAll())
                    {
                    if (savedDevice1.getAddress().equals(savedDevice.getAddress()))
                        {
                        isAlreadyAdded = true;
                        }
                    }
                if (!isAlreadyAdded)
                    {
                    savedDeviceBox.put(savedDevice);
                    }
                finish();
                }
            };
        scannedDevicesListRecyclerView.setAdapter(scannedDevicesListRecyclerViewAdapter);
        savedDeviceBox = ((EntryPoint) getApplication()).getBoxStore().boxFor(SavedDevice.class);
        }

    private void setUpPermissionsAndConfigurations()
        {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_LOCATION);
            }
        else
            {
            setUpBluetoothAdapter();
            }
        }

    private void setUpViewIds()
        {
        scannedDevicesListRecyclerView = findViewById(R.id.scanned_devices_list_recycler_view_scan_devices_activity);
        }

    abstract class ScannedDevicesListRecyclerViewAdapter
            extends
            RecyclerView.Adapter<ScannedDevicesListRecyclerViewAdapter.ScannedDeviceViewHolder>
        {
        List<BluetoothDevice> bluetoothDeviceList;
        Context context;

        ScannedDevicesListRecyclerViewAdapter(
                Context context)
            {
            this.bluetoothDeviceList = new ArrayList<>();
            this.context = context;
            }

        void setBluetoothDeviceList(
                List<BluetoothDevice> bluetoothDeviceList)
            {
            this.bluetoothDeviceList = bluetoothDeviceList;
            notifyDataSetChanged();
            }

        void addBluetoothDevice(
                BluetoothDevice bluetoothDevice)
            {
            if (!bluetoothDeviceList.contains(bluetoothDevice))
                {
                bluetoothDeviceList.add(bluetoothDevice);
                }
            notifyDataSetChanged();
            }

        void clearDeviceList()
            {
            this.bluetoothDeviceList.clear();
            notifyDataSetChanged();
            }

        @NonNull
        @Override
        public ScannedDeviceViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType)
            {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scanned_device, parent, false);
            return new ScannedDeviceViewHolder(view);
            }

        @Override
        public void onBindViewHolder(
                @NonNull ScannedDeviceViewHolder holder,
                int position)
            {
            final BluetoothDevice bluetoothDevice = bluetoothDeviceList.get(position);
            holder.deviceNameTextView.setText(bluetoothDevice.getName());
            holder.deviceAddressTextView.setText(bluetoothDevice.getAddress());
            if (bluetoothDevice.getName() == null)
                {
                holder.deviceNameTextView.setText("Unknown Device");
                }
            holder.cardView.setOnClickListener(v -> onDeviceSelected(bluetoothDevice));
            }

        abstract void onDeviceSelected(
                BluetoothDevice bluetoothDevice);

        @Override
        public int getItemCount()
            {
            return bluetoothDeviceList.size();
            }

        void removeBluetoothDevice(
                BluetoothDevice bluetoothDevice)
            {
            this.bluetoothDeviceList.remove(bluetoothDevice);
            notifyDataSetChanged();
            }

        class ScannedDeviceViewHolder
                extends
                RecyclerView.ViewHolder
            {
            TextView deviceNameTextView, deviceAddressTextView;
            CardView cardView;

            ScannedDeviceViewHolder(
                    View view)
                {
                super(view);
                deviceNameTextView = view.findViewById(R.id.device_name);
                deviceAddressTextView = view.findViewById(R.id.device_address);
                this.cardView = view.findViewById(R.id.item_scanned_device_card_view);
                }

            }
        }

    }