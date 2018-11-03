package com.sayingsdevelopers.ble;

import android.app.*;

import com.polidea.rxandroidble2.*;
import com.polidea.rxandroidble2.exceptions.*;
import com.polidea.rxandroidble2.internal.*;
import com.sayingsdevelopers.ble.models.MyObjectBox;

import io.objectbox.*;
import io.reactivex.exceptions.*;
import io.reactivex.plugins.*;


public class EntryPoint extends Application
    {
    public static BoxStore boxStore;
    public static RxBleClient rxBleClient;
    public BoxStore getBoxStore()
        {
        return boxStore;
        }

    @Override
    public void onCreate()
        {
        super.onCreate();
        rxBleClient = RxBleClient.create(getApplicationContext());
        boxStore = MyObjectBox.builder().androidContext(getApplicationContext()).build();
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
        RxJavaPlugins.setErrorHandler(error -> {
        if (error instanceof UndeliverableException && error.getCause() instanceof BleException) {
        return; // ignore BleExceptions as they were surely delivered at least once
        }
        // add other custom handlers if needed
        });
        }

    public static RxBleClient getRxBleClient()
        {
        return rxBleClient;
        }
    }
