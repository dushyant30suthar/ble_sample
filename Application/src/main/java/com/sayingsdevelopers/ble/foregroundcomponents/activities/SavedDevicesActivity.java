package com.sayingsdevelopers.ble.foregroundcomponents.activities;

import android.Manifest;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sayingsdevelopers.ble.EntryPoint;
import com.sayingsdevelopers.ble.R;
import com.sayingsdevelopers.ble.backgroundcomponents.ConnectionService;
import com.sayingsdevelopers.ble.interfaces.BluetoothConnectionCallbacksListener;
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

public class SavedDevicesActivity
        extends
        AppCompatActivity
        implements
        ChecksPerformedListener,
        BluetoothConnectionCallbacksListener,
        BluetoothScanResultCallbacksListener
    {
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_ACCESS_LOCATION = 2;
    List<SavedDevice> savedDeviceList;
    Box<SavedDevice> savedDeviceBox;
    ConnectionService connectionService;
    RecyclerView savedDevicesListRecyclerView;
    SavedDevicesListRecyclerViewAdapter savedDevicesListRecyclerViewAdapter;

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

    public void changeInConnection(
            String address,
            boolean connected)
        {
        savedDevicesListRecyclerViewAdapter.setSavedDevices(savedDeviceBox.getAll());
        }

    @Override
    public void onBackPressed()
        {
        stopService(new Intent(SavedDevicesActivity.this, ConnectionService.class));
        super.onBackPressed();
        }

    @Override
    protected void onPause()
        {
        super.onPause();
        if (connectionService != null)
            {
            connectionService.removeBluetoothScanResultsCallbacksListener(this);
            connectionService.removeConnectionCallbackListener(this);
            }
        }

    @Override
    protected void onResume()
        {
        super.onResume();
        Intent intent = new Intent(SavedDevicesActivity.this, ConnectionService.class);
        savedDevicesListRecyclerViewAdapter.setSavedDevices(savedDeviceBox.getAll());
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
                Toast.makeText(SavedDevicesActivity.this, "Sorry! App wouldn't work for you", Toast.LENGTH_LONG).show();
                }
            }
            }
        }

    @Override
    protected void onCreate(
            Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_devices);
        setUpViewIdsAndInitialTouchUps();
        }

    @Override
    protected void onStop()
        {
        super.onStop();
        if (serviceConnection != null)
            {
            unbindService(serviceConnection);
            }
        }

    @Override
    protected void onDestroy()
        {
        super.onDestroy();
        }

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu)
        {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
        }

    @Override
    public boolean onOptionsItemSelected(
            MenuItem item)
        {
        switch (item.getItemId())
            {
            case R.id.menu_item_scan_devices:
                startActivity(new Intent(this, ScanDevicesActivity.class));
                break;
            }
        return true;
        }

    @Override
    public void onDeviceAvailable(
            BluetoothDevice bluetoothDevice)
        {
        Log.d(bluetoothDevice.getName(), "isAvailable : " + true);
        if (connectionService.getConnectionManager(bluetoothDevice.getAddress()) == null)
            {
            for (SavedDevice savedDevice : savedDevicesListRecyclerViewAdapter.savedDevices)
                {
                if (savedDevice.getAddress().equals(bluetoothDevice.getAddress()))
                    {
                    connectionService.connectDevice(bluetoothDevice.getAddress());
                    break;
                    }
                }
            }
        }

    @Override
    public void onDeviceUnAvailable(
            BluetoothDevice bluetoothDevice)
        {
        if (bluetoothDevice != null)
            {
            Log.d(bluetoothDevice.getName(), "isAvailable : " + false);
            if (connectionService.getConnectionManager(bluetoothDevice.getAddress()) != null)
                {
                for (SavedDevice savedDevice : savedDevicesListRecyclerViewAdapter.savedDevices)
                    {
                    if (savedDevice.getAddress().equals(bluetoothDevice.getAddress()))
                        {
                        connectionService.disconnectDevice(bluetoothDevice.getAddress());
                        break;
                        }
                    }
                }
            }
        }

    @Override
    public void onDeviceConnected(
            BluetoothDevice bluetoothDevice)
        {
        changeInConnection(bluetoothDevice.getAddress(), true);
        }

    @Override
    public void onDeviceDisconnected(
            BluetoothDevice bluetoothDevice)
        {
        changeInConnection(bluetoothDevice.getAddress(), false);
        }

    @Override
    public void onEverythingIsChecked(
            boolean passed)
        {
        if (passed)
            {
            savedDeviceList = savedDeviceBox.getAll();
            savedDevicesListRecyclerViewAdapter.setSavedDevices(savedDeviceList);
            connectionService.addBluetoothScanCallbacksListener(this);
            connectionService.addBluetoothConnectionCallbackListener(this);
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
                connectionService.everyThingIsChecked(SavedDevicesActivity.this, true);
                }
            }
        else
            {
            connectionService.everyThingIsChecked(SavedDevicesActivity.this, true);
            }
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

    private void setUpViewIdsAndInitialTouchUps()
        {
        savedDevicesListRecyclerView = findViewById(R.id.saved_devices_list_recycler_view_scan_devices_activity);
        savedDevicesListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedDevicesListRecyclerViewAdapter = new SavedDevicesListRecyclerViewAdapter(this);
        savedDevicesListRecyclerView.setAdapter(savedDevicesListRecyclerViewAdapter);
        savedDeviceBox = ((EntryPoint) getApplication()).getBoxStore().boxFor(SavedDevice.class);
        }

    class SavedDevicesListRecyclerViewAdapter
            extends
            RecyclerView.Adapter<SavedDevicesListRecyclerViewAdapter.SavedDeviceViewHolder>
        {
        List<SavedDevice> savedDevices;
        Context context;

        SavedDevicesListRecyclerViewAdapter(
                Context context)
            {
            this.savedDevices = new ArrayList<>();
            this.context = context;
            }

        void addSavedDevice(
                SavedDevice savedDevice)
            {
            if (!savedDeviceList.contains(savedDevice))
                {
                savedDeviceList.add(savedDevice);
                }
            notifyDataSetChanged();
            }

        SavedDevice getItem(
                String address)
            {
            for (int i = 0; i < this.savedDevices.size(); i++)
                {
                if (savedDevices.get(i).getAddress().equals(address))
                    {
                    return savedDevices.get(i);
                    }
                }
            return null;
            }

        int getItemPosition(
                String address)
            {
            for (int i = 0; i < this.savedDevices.size(); i++)
                {
                if (savedDevices.get(i).getAddress().equals(address))
                    {
                    return i;
                    }
                }
            return 0;
            }

        SavedDevice getSavedDevice(
                int index)
            {
            return savedDevices.get(index);
            }

        @NonNull
        @Override
        public SavedDevicesListRecyclerViewAdapter.SavedDeviceViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType)
            {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_device, parent, false);
            return new SavedDevicesListRecyclerViewAdapter.SavedDeviceViewHolder(view);
            }

        @Override
        public void onBindViewHolder(
                @NonNull final SavedDevicesListRecyclerViewAdapter.SavedDeviceViewHolder holder,
                final int position)
            {
            final SavedDevice savedDevice = savedDevices.get(position);
            holder.deviceNameTextView.setText(savedDevice.getDeviceName());
            holder.connectionStatusTextView.setText(savedDevice.getConnectionStatus());
            if (savedDevice.getDeviceName() == null)
                {
                holder.deviceNameTextView.setText("Unknown Device");
                }
            holder.cardView.setOnClickListener(v ->
            {
            // todo : make connection attempt

            });

            holder.removeButton.setOnClickListener(v ->
            {
            if (connectionService.isDeviceConnected(savedDevice.getAddress()))
                {
                connectionService.disconnectDevice(savedDevice.getAddress());
                }
            savedDeviceBox.remove(savedDevice);
            setSavedDevices(savedDeviceBox.getAll());
            });
            holder.disconnectButton.setOnClickListener(v ->
            {
            if (connectionService.isDeviceConnected(savedDevice.getAddress()))
                {
                connectionService.disconnectDevice(savedDevice.getAddress());
                }
            });

            }


        @Override
        public int getItemCount()
            {
            return savedDevices.size();
            }

        void removeSavedDevice(
                SavedDevice savedDevice)
            {
            this.savedDevices.remove(savedDevice);
            notifyDataSetChanged();
            }

        void setSavedDevices(
                List<SavedDevice> savedDevices)
            {
            this.savedDevices = savedDevices;
            notifyDataSetChanged();
            }

        class SavedDeviceViewHolder
                extends
                RecyclerView.ViewHolder
            {
            TextView deviceNameTextView, connectionStatusTextView;
            Button disconnectButton, removeButton;
            CardView cardView;
            SavedDeviceViewHolder(
                    View view)
                {
                super(view);
                deviceNameTextView = view.findViewById(R.id.device_name);
                connectionStatusTextView = view.findViewById(R.id.device_connection_status);
                disconnectButton = view.findViewById(R.id.disconnect_button);
                removeButton = view.findViewById(R.id.remove_button);
                this.cardView = view.findViewById(R.id.item_saved_device_card_view);
                }

            }
        }
    }
