package com.sayingsdevelopers.ble.models;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class SavedDevice
    {
    @Id
    public long id;
    private String connectionStatus;
    private String deviceName;
    private String address;

    public String getAddress()
        {
        return address;
        }

    public void setAddress(
            String address)
        {
        this.address = address;
        }

    public String getConnectionStatus()
        {
        return connectionStatus;
        }

    public void setConnectionStatus(
            String connectionStatus)
        {
        this.connectionStatus = connectionStatus;
        }

    public String getDeviceName()
        {
        return deviceName;
        }

    public void setDeviceName(
            String deviceName)
        {
        this.deviceName = deviceName;
        }

    public long getId()
        {
        return id;
        }

    public void setId(
            long id)
        {
        this.id = id;
        }
    }
