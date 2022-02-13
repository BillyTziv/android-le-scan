package com.example.recyclerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // UI elements.
    Button scanButton;                          // SCAN button at the bottom of the screen.
    private ArrayList<Device> availableDeviceList;
    private RecyclerView recyclerView;
    private recyclerAdapter.RecyclerViewClickListener listener;

    // Global variables to talk in all the classes.
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    LeDeviceListAdapter leDeviceListAdapter;    // A list of all the available devices.
    BluetoothGatt bluetoothGatt;

    // Control variables to control the flow of code.
    private boolean isScanning;                 // A control variable to check if we are currently scanning for devices.
    private Handler handler = new Handler();

    // =========================================================================================
    // BluetoothGatt CALLBACK
    // =========================================================================================
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d("VT MSG:", "onConnectionStageChange() function called.");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                //broadcastUpdate(ACTION_GATT_CONNECTED);
                // Attempts to discover services after successful connection.

                //boolean bthServices = bluetoothGatt.discoverServices();
                //Log.d("MESSAGE:", "onServicesDiscovered received: " + bthServices);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                //broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("VT MSG:", "onServicesDiscovered() function called.");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                //Log.d("MESSAGE:", "onServicesDiscovered received: " + status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        leDeviceListAdapter = new LeDeviceListAdapter();
        scanButton = findViewById(R.id.button0);
        scanButton.setOnClickListener(view -> {
            if (areAllPermissionsGranted()) {
                Toast.makeText(MainActivity.this, "All Permission already granted", Toast.LENGTH_SHORT).show();
            } else {
                askAllPermissions();
            }
            FindDevices();
        });
        //................
        recyclerView = findViewById(R.id.recyclerView);
        availableDeviceList = new ArrayList<>();
    }

    private void setAdapter() {//Create Instance of Recycle Adapter
        setOnClickListner();
        recyclerAdapter adapter = new recyclerAdapter(availableDeviceList, listener);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // This thing puts the device in the list.
        recyclerView.setAdapter(adapter);
    }

    private void setOnClickListner() {
        listener = new recyclerAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Log.d("The device is: ", String.valueOf(availableDeviceList.get(position).getAddress()));

                // Create an instance of BluetoothService class which should have access to all of its methods.
                BluetoothService mybth = new BluetoothService();

                String myDevice = availableDeviceList.get(position).getAddress();
                Context myContext = getApplicationContext();
                BluetoothAdapter myBthAdapter = bluetoothAdapter;

                BluetoothGatt bthGatt = mybth.connectDevice( myDevice, myContext, myBthAdapter );
                Log.d("REPORT", String.valueOf(bthGatt));

//                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(availableDeviceList.get(position).getAddress());
//                bluetoothGatt = device.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
//
//                boolean bthServices = bluetoothGatt.discoverServices();
//                bluetoothGatt.connect();
//                Log.d("Connected with: ", String.valueOf(bluetoothGatt));              // Unused
//                Log.d("ALL OK?", String.valueOf(bthServices));
            }
        };
    }

    public void FindDevices(){
        // Device scan callback.
        ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    leDeviceListAdapter.addDevice(result.getDevice());
                    leDeviceListAdapter.notifyDataSetChanged();
                }
            };

        //Scan Process
        if (!isScanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, 10000);

            isScanning = true;
            Log.d("Scan", "...Scan is Working");
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            Log.d("Scan", "...Scan stop scannnig? wtf?");
            isScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    public void askAllPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndAdd(permissionsList, Manifest.permission.BLUETOOTH_SCAN);
            checkAndAdd(permissionsList, Manifest.permission.BLUETOOTH_CONNECT);
            checkAndAdd(permissionsList, Manifest.permission.BLUETOOTH_ADVERTISE);
        }
        checkAndAdd(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION);
        checkAndAdd(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (!permissionsList.isEmpty()) {
            String[] temp = new String[permissionsList.size()];
            ActivityCompat.requestPermissions(this, permissionsList.toArray(temp), 903);
        }
    }

    public void checkAndAdd(ArrayList<String> permissionList, String permission) {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED)) {
            permissionList.add(permission);
        }
    }

    public boolean areAllPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED);
        }
        return (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 903) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, permissions[i] + " Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, permissions[i] + " Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        private void broadcastUpdate(Object actionGattConnected) {
        }

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }
        public void addDevice(BluetoothDevice device) {
            Toast.makeText(MainActivity.this, "Searching...", Toast.LENGTH_SHORT).show();

            View myView;
            ViewHolder myViewHolder;

            // New device found! Add it to the list.
            if(!mLeDevices.contains(device)) {
                availableDeviceList.add(new Device(String.valueOf(device)));
                mLeDevices.add(device); // device is the Bluetooth MAC address.         // Unused
                Log.d("New Device with ID: ", String.valueOf(device));              // Unused
                setAdapter();//set Adapter for Recycle View
            }
        }
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        public void clear() {
            mLeDevices.clear();
        }
        @Override
        public int getCount() {
            //Toast.makeText(MainActivity.this, "Total Devices Found: " + mLeDevices.size(), Toast.LENGTH_SHORT).show();
            return mLeDevices.size();
        }
        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }

    static class ViewHolder {
        String deviceName;
        TextView deviceAddress;
    }

//    class BluetoothService extends Service {
//        public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
//        private Binder binder = new LocalBinder();
//
//        class LocalBinder extends Binder {
//            public BluetoothService getService() {
//                return BluetoothService.this;
//            }
//        }
//
////        @Nullable
////        @Override
////        public IBinder onBind(Intent intent) {
////            return binder;
////        }
//
//        private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
//            @Override
//            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    // successfully connected to the GATT Server
//                    //connectionState = STATE_CONNECTED;
//                    //broadcastUpdate(ACTION_GATT_CONNECTED);
//                    // Attempts to discover services after successful connection.
//                    //bluetoothGatt.discoverServices();
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    // disconnected from the GATT Server
//                    //connectionState = STATE_DISCONNECTED;
//                    //broadcastUpdate(ACTION_GATT_DISCONNECTED);
//                }
//            }
//
//            @Override
//            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
//                } else {
//                    //Log.w(TAG, "onServicesDiscovered received: " + status);
//                }
//            }
//        };
//
////        public List<BluetoothGattService> getSupportedGattServices() {
////            if (bluetoothGatt == null) return null;
////            return bluetoothGatt.getServices();
////        }
//
//        public boolean connectDevice( String myDevice, Context contextActivity ) {
//            Log.d("Trying to connect: ", myDevice);
//            BluetoothDevice device = bluetoothAdapter.getRemoteDevice( myDevice );
//            bluetoothGatt = device.connectGatt(contextActivity, false, bluetoothGattCallback);
//
//            boolean bthServices = bluetoothGatt.discoverServices();
//            boolean xyz = bluetoothGatt.connect();
//            Log.d("Connected with: ", String.valueOf(xyz));              // Unused
//            Log.d("ALL OK?", String.valueOf(bthServices));
//
//            return xyz;
//        }
//    }
    // ===========================================================================================
    // BLUETOOTHLESERVICE
    // ===========================================================================================
//    class BluetoothLeService extends Service {
//        private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
//            @Override
//            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    // successfully connected to the GATT Server
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    // disconnected from the GATT Server
//                }
//            }
//        };
//
//        private Binder binder = new LocalBinder();
//
//        @Nullable
//        @Override
//        public IBinder onBind(Intent intent) {
//            return binder;
//        }
//
//        class LocalBinder extends Binder {
//            public BluetoothLeService getService() {
//                return BluetoothLeService.this;
//            }
//        }
//    }
}