/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsung.slsi.hpatchhost;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private final HashMap<String, BluetoothGatt> mAddressGattMap = new HashMap<>();
    private final HashMap<BluetoothGatt, String> mGattAddressMap = new HashMap<>();

    public final static String ACTION_GATT_CONNECTED = "com.samsung.slsi.SPATCH.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.samsung.slsi.SPATCH.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.samsung.slsi.SPATCH.ACTION_GATT_SERVICES_DISCOVERED";

    public final static String ACTION_DATA_AVAILABLE_SPATCH_READ = "com.samsung.slsi.SPATCH.ACTION_DATA_AVAILABLE_SPATCH_READ";
    public final static String ACTION_DATA_AVAILABLE_SPATCH_WRITE = "com.samsung.slsi.SPATCH.ACTION_DATA_AVAILABLE_SPATCH_WRITE";

    public final static String ACTION_DATA_AVAILABLE_SPATCH_HEART_RATE_MEASUREMENT = "com.samsung.slsi.SPATCH.ACTION_DATA_AVAILABLE_SPATCH_HEART_RATE_MEASUREMENT";

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public final static String ACTION_BLE_ADDRESS = "com.samsung.slsi.SPATCH.ACTION_BLE_ADDRESS";
    public final static String MEASURED_HEART_RATE_VALUE = "com.samsung.slsi.SPATCH.MEASURED_HEART_RATE_VALUE";

    BluetoothGatt mGatt;
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            mGatt.readRemoteRssi();
        }
    };
    Timer timer = new Timer();

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String address = mGattAddressMap.get(gatt);
            mGatt = gatt;
            if (address != null) {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");

                    intentAction = ACTION_GATT_CONNECTED;
                    broadcastUpdate(intentAction, address);

                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
                    //timer.schedule(task, 0, 1000);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //timer.cancel();
                    Log.i(TAG, "Disconnected from GATT server.");

                    intentAction = ACTION_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction, address);

                    synchronized (mAddressGattMap) {
                        mAddressGattMap.remove(address);
                        mGattAddressMap.remove(gatt);
                    }

                    gatt.close();
                }
            }
        }

        /*@Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
            String time = sdf.format(date);
            //writeTextFile(Environment.getExternalStorageDirectory() + "/MyHeartFit/", time + "_RSSI.txt", String.valueOf(rssi));
            super.onReadRemoteRssi(gatt, rssi, status);
        }*/

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String address = mGattAddressMap.get(gatt);
            if (address != null) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, address);
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String address = mGattAddressMap.get(gatt);
            if (address != null) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    String uuid = characteristic.getUuid().toString();
                    //Log.d(TAG, "onCharacteristicRead: " + uuid);
                    if (BleUuid.CHAR_SPATCH_READ.equals(uuid)) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE_SPATCH_READ, address, characteristic);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String address = mGattAddressMap.get(gatt);
            if (address != null) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if(BleUuid.CHAR_SPATCH_WRITE.equals(characteristic.getUuid().toString())) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE_SPATCH_WRITE, address, characteristic);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String address = mGattAddressMap.get(gatt);
            if (address != null) {
                String uuid = characteristic.getUuid().toString();
                //Log.d(TAG, "onCharacteristicChanged: " + uuid);
                if (BleUuid.CHAR_SPATCH_READ.equals(uuid)) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE_SPATCH_READ, address, characteristic);
                } else if (BleUuid.CHAR_HEART_RATE_MEASUREMENT.equals(uuid)) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE_SPATCH_HEART_RATE_MEASUREMENT, address, characteristic);
                }
            }
        }
    };

    public void writeTextFile(final String foldername, final String filename, final String contents) {
        try {
            File dir = new File(foldername);
            if(!dir.exists()) {
                dir.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(foldername + "/" + filename, true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String time = sdf.format(date);
            writer.write(time + " " + contents + "_");
            writer.flush();
            writer.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastUpdate(final String action, String address) {
        final Intent intent = new Intent(action);
        intent.putExtra(ACTION_BLE_ADDRESS, address);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, String address, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        intent.putExtra(ACTION_BLE_ADDRESS, address);
        if (BleUuid.CHAR_SPATCH_READ.equals(characteristic.getUuid().toString())) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(ACTION_DATA_AVAILABLE_SPATCH_READ, data);
            }
        } else if (BleUuid.CHAR_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                //Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                //Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            //Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(MEASURED_HEART_RATE_VALUE, String.valueOf(heartRate));
        }

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        BluetoothGatt gatt = mAddressGattMap.get(address);
        if (gatt != null) {
            return gatt.connect();
        } else {
            Log.d(TAG, "Trying to get the device from " + address);
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }

            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            gatt = device.connectGatt(this, false, mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");

            mAddressGattMap.put(address, gatt);
            mGattAddressMap.put(gatt, address);

            return true;
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(String address) {
        BluetoothGatt gatt = mAddressGattMap.get(address);
        if (mBluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        for (BluetoothGatt gatt : mGattAddressMap.keySet()) {
            gatt.close();
        }
        mAddressGattMap.clear();
        mGattAddressMap.clear();
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(String address, BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = mAddressGattMap.get(address);
        if (mBluetoothAdapter == null || gatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(String address, BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = mAddressGattMap.get(address);
        if (mBluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        //Log.d(TAG, "writeCharacteristic");
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        gatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(String address, BluetoothGattCharacteristic characteristic, boolean enabled) {
        BluetoothGatt gatt = mAddressGattMap.get(address);
        if (mBluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        gatt.setCharacteristicNotification(characteristic, enabled);

        if (BleUuid.CHAR_SPATCH_READ.equals(characteristic.getUuid().toString())) {
            BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIG, BluetoothGattDescriptor.PERMISSION_READ);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
//*
        else if (BleUuid.CHAR_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid().toString())) {
            try {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//*/
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices(String address) {
        BluetoothGatt gatt = mAddressGattMap.get(address);
        if (gatt == null)
            return null;
        else
            return gatt.getServices();
    }

}
