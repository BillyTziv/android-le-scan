package com.example.recyclerview;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;

public class BluetoothService extends Service {
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public BluetoothGatt myBthGatt;
    private int connectionState;    // 1: connected, 0:disconnected.

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("MSG", "Function onConnectionStateChange - STATE_CONNECTED");

                // successfully connected to the GATT Server
                connectionState = 1;
                //broadcastUpdate("ACTION_GATT_CONNECTED");

                // Attempts to discover services after successful connection.
                Log.d("VT_MSG:SERVICES_FOUND:", String.valueOf(gatt.discoverServices()));

                List<BluetoothGattService> bthServiceList = getSupportedGattServices( gatt );
                Log.d("VT_MSG:SERVICES_LIST:", String.valueOf(bthServiceList.size()));

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("MSG", "Function onConnectionStateChange - STATE_DISCONNECTED");

                // disconnected from the GATT Server
                connectionState = 0;
                //broadcastUpdate("ACTION_GATT_DISCONNECTED");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("MSG", "Function onServicesDiscovered - GATT_SUCCESS");
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                gatt.discoverServices();
            } else {
                Log.d("MSG", "Function onServicesDiscovered - GATT_FAILED");
                //Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };

    public List<BluetoothGattService> getSupportedGattServices( BluetoothGatt bthGatt ) {
        if (bthGatt == null) return null;
        return bthGatt.getServices();
    }

    /**
     *
     * @param myDevice
     * @param contextActivity
     *
     * @return BluetoothGatt
     */
    public BluetoothGatt connectDevice( String myDevice, Context contextActivity, BluetoothAdapter bthAdapter ) {
        Log.d("Trying to connect: ", myDevice);
        BluetoothDevice device = bthAdapter.getRemoteDevice( myDevice );

        // Connect to the GATT server.
        myBthGatt = device.connectGatt(contextActivity, false, bluetoothGattCallback);

        //boolean xyz = myBthGatt.connect( myDevice );
        //Log.d("Connected with: ", String.valueOf(xyz));              // Unused

        return myBthGatt;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ============================================================================
    // ENAS ENALAKTIKOS TROPOS GIA TO OVERRIDE POU PZIEIS
    // ============================================================================
    //    private Binder binder = new MainActivity.BluetoothService.LocalBinder();
    //    class LocalBinder extends Binder {
    //        public MainActivity.BluetoothService getService() {
    //            return MainActivity.BluetoothService.this;
    //        }
    //    }
    //        @Nullable
    //        @Override
    //        public IBinder onBind(Intent intent) {
    //            return binder;
    //        }
}