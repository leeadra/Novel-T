/*
 * Sensor Product Development Team, System LSI division.
 * Copyright (c) 2014-2017 Samsung Electronics, Inc.
 * All right reserved.
 *
 * This software is the confidential and proprietary information
 * of Samsung Electronics, Inc. (Confidential Information). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Samsung Electronics.
*/
/**
 *******************************************************************************
 * @file		HPatchHostAndroid.java
 * @brief		Android Device's BLE and Storage Access Support
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/21
 *
 * <b>revision history :</b>
 * - 2016/11/21 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchhost;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.samsung.slsi.BLEDeviceObserver;
import com.samsung.slsi.HPatchDeviceBLEInfo;
import com.samsung.slsi.HPatchException;
import com.samsung.slsi.HPatchHostBLE;
import com.samsung.slsi.PermissionResultHandler;
import com.samsung.slsi.HPatchBLEHeartRateObserver;
import com.samsung.slsi.HPatchBLECharacteristicObserver;
import com.samsung.slsi.HPatchBLEException;
import com.samsung.slsi.HPatchHostOS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class HPatchHostAndroid implements PermissionResultHandler, HPatchHostOS, HPatchHostBLE, BLEDeviceObserver {
    private static String TAG = HPatchHostAndroid.class.getSimpleName();

    private Activity activity;
    public Activity getActivity() { return activity; }
    private Context context;

    private String temporaryPath;

    private BLEPermissionManager blePermissionManager;
    private StoragePermissionManager storagePermissionManager;

    private BLEScanner bleScanner;
    private final List<BLEDeviceObserver> bleDeviceObservers = new LinkedList<>();

    private BluetoothLeService bluetoothLeService;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        blePermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        storagePermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class BLEDevice {
        HPatchDeviceBLEInfo hPatchDeviceBLEInfo;

        BluetoothGattCharacteristic mGattCharacteristicRead;
        BluetoothGattCharacteristic mGattCharacteristicWrite;
        BluetoothGattCharacteristic mGattCharacteristicFlowCtrl;

        BluetoothGattCharacteristic mGattCharacteristicHeartRateMeasurement;

        final List<HPatchBLECharacteristicObserver> bleCharacteristicObservers = new LinkedList<>();
        final List<HPatchBLEHeartRateObserver> bleHeartRateObservers = new LinkedList<>();

        BLEDevice(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
            this.hPatchDeviceBLEInfo = hPatchDeviceBLEInfo;
        }
    }

    private final HashMap<String, BLEDevice> addressBLEDeviceMap = new HashMap<>();


    private File makeDirectory(String dirPath) throws FileNotFoundException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new FileNotFoundException(dirPath);
            }
        }
        return dir;
    }

    private File makeFile(File dir , String filePath) throws IOException {
        File file = null;
        if (dir.isDirectory()) {
            file = new File(filePath);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException(filePath);
                }
            }
        }
        return file;
    }


    public HPatchHostAndroid(Activity activity, String temporaryPath) throws HPatchException {
        this.activity = activity;
        this.context = activity.getApplicationContext();

        this.temporaryPath = temporaryPath;

        blePermissionManager = new BLEPermissionManager(activity);
        storagePermissionManager = new StoragePermissionManager(activity);
    }

    @Override
    public PermissionResultHandler getPermissionResultHandler() {
        return this;
    }

    @Override
    public int getRemainedStorageMBSize() {
        File external = Environment.getExternalStorageDirectory();
        long freeBytes = external.getFreeSpace();
        return (int)(freeBytes / 1000 / 1000);
    }

    @Override
    public String getTemporaryPath() {
        return temporaryPath;
    }

    @Override
    public void storeFile(String path, String fileName, byte[] data, boolean isAppend)
            throws IOException {
        if (!storagePermissionManager.isWriteGranted()) {
            throw new IOException("WRITE permission denied");
        }

        makeDirectory(path);
        FileOutputStream fos;

        String fullPath;
        if (path.charAt(path.length() - 1) == '/') {
            fullPath = path + fileName;
        } else {
            fullPath = path + "/" + fileName;
        }

        fos = new FileOutputStream(fullPath, isAppend);
        fos.write(data);
        fos.close();
    }

    @Override
    public void storeFile(String path, String fileName, byte[] data, int startIndex)
            throws IOException {
        if (!storagePermissionManager.isWriteGranted()) {
            throw new IOException("WRITE permission denied");
        }

        makeDirectory(path);

        String fullPath;
        if (path.charAt(path.length() - 1) == '/') {
            fullPath = path + fileName;
        } else {
            fullPath = path + "/" + fileName;
        }

        File file = new File(fullPath);
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, startIndex, data.length);
        buffer.put(data);
    }

    @Override
    public byte[] restoreFile(String path, String fileName, int index, int size)
            throws IOException {
        /*if (!storagePermissionManager.isReadGranted()) {
            throw new IOException("READ permission denied");
        }

        FileInputStream fis;
        byte[] data = null;

        String fullPath;
        if (path.charAt(path.length() - 1) == '/') {
            fullPath = path + fileName;
        } else {
            fullPath = path + "/" + fileName;
        }

        fis = new FileInputStream(fullPath);
        long availableSize = fis.available();
        if (size <= 0) {
            size = (int)availableSize;
        }

        if (availableSize < index) {
            size = 0;
        } else if (availableSize < index + size) {
            size = (int)(index + size - availableSize);
        }

        long skipped = fis.skip(index);
        if (skipped < index) {
            size = 0;
        }

        data = new byte[size];
        fis.read(data);

        fis.close();*/

        return null;
    }

    @Override
    public boolean removeFile(String path, String fileName) {
        boolean isDeleted = true;

        File dir = new File(path);
        if (dir.exists()) {
            File file = new File(fileName);
            if (file.exists()) {
                isDeleted = file.delete();
            }
        }

        return isDeleted;
    }

    @Override
    public int getFileSize(String path, String fileName) throws IOException {
        if (!storagePermissionManager.isReadGranted()) {
            throw new IOException("READ permission denied");
        }

        FileInputStream fis;
        fis = new FileInputStream(path + fileName);
        return fis.available();
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();

            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    public void setPreference(String key, String value) {
        if (this.activity != null) {
            SharedPreferences sharedPref = this.activity.getPreferences(Context.MODE_PRIVATE);
            if (sharedPref != null) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if (editor != null) {
                    editor.putString(key, value);
                    editor.apply();
                }
            }
        }
    }

    @Override
    public String getPreference(String key) {
        String value = null;

        if (this.activity != null) {
            SharedPreferences sharedPref = this.activity.getPreferences(Context.MODE_PRIVATE);
            if (sharedPref != null) {
                value = sharedPref.getString(key, null);
            }
        }

        return value;
    }

    @Override
    public boolean isBLEScanning() {
        return bleScanner.isBLEScanning();
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int rssi, final byte[] scanRecord) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String name = bluetoothDevice.getName();
                    String address = bluetoothDevice.getAddress();

                    Log.d(TAG, "Detected: " + name + " (" + address + ")");

                    HPatchDeviceBLEInfo hPatchDeviceBLEInfo = new HPatchDeviceBLEInfo(
                            name,
                            address,
                            scanRecord,
                            rssi);

                    synchronized (bleDeviceObservers) {
                        for (BLEDeviceObserver observer : bleDeviceObservers) {
                            observer.onBLEDeviceDetected(hPatchDeviceBLEInfo);
                        }
                    }
                }
            });
        }
    };

    @Override
    public void startBLEScanning() throws HPatchException {
        if (!isBLEEnabled()) {
            enableBLE();
            if (!isBLEEnabled()) {
                throw new HPatchException("BLE is not enabled");
            }
        }

        if (!bleScanner.isBLEScanning()) {
            Log.d(TAG, "Start BLE Scanning");
            bleScanner.startLeScan(leScanCallback);
        }
    }

    @Override
    public void stopBLEScanning() {
        Log.d(TAG, "Stop BLE Scanning");
        bleScanner.stopLeScan();
    }

    @Override
    public void connectBLE(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) throws HPatchBLEException {
        Log.d(TAG, "Connect BLE:" + hPatchDeviceBLEInfo.address);
        addressBLEDeviceMap.put(hPatchDeviceBLEInfo.address, new BLEDevice(hPatchDeviceBLEInfo));
        if (!bluetoothLeService.connect(hPatchDeviceBLEInfo.address)) {
            throw new HPatchBLEException(hPatchDeviceBLEInfo, "Fail to connect");
        }
    }

    @Override
    public void disconnectBLE(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
        Log.d(TAG, "Disconnect BLE:" + hPatchDeviceBLEInfo.address);
        bluetoothLeService.disconnect(hPatchDeviceBLEInfo.address);
    }

    @Override
    public void addBLEDeviceObserver(BLEDeviceObserver observer) {
        synchronized (bleDeviceObservers) {
            bleDeviceObservers.add(observer);
        }
    }

    @Override
    public void removeBLEDeviceObserver(BLEDeviceObserver observer) {
        synchronized (bleDeviceObservers) {
            bleDeviceObservers.remove(observer);
        }
    }

    @Override
    public void write(String address, byte[] packet) throws HPatchBLEException {
        BLEDevice bleDevice = addressBLEDeviceMap.get(address);
        if (bleDevice != null) {
            if (bleDevice.mGattCharacteristicWrite != null) {
                bleDevice.mGattCharacteristicWrite.setValue(packet);

                if (bluetoothLeService == null) {
                    throw new HPatchBLEException(bleDevice.hPatchDeviceBLEInfo,
                            "BluetoothLeService is not initialized yet"
                                    + ": " + bleDevice.hPatchDeviceBLEInfo.name
                                    + ", " + bleDevice.hPatchDeviceBLEInfo.address
                                    + ", " + bleDevice.hPatchDeviceBLEInfo.rssi);
                } else {
                    bluetoothLeService.writeCharacteristic(address, bleDevice.mGattCharacteristicWrite);
                }
            } else {
                throw new HPatchBLEException(bleDevice.hPatchDeviceBLEInfo, "Fail to get GATT Characteristic Write");
            }
        }
    }

    @Override
    public void addBLEReadObserver(String address, HPatchBLECharacteristicObserver observer) {
        BLEDevice bleDevice = addressBLEDeviceMap.get(address);
        if (bleDevice != null) {
            bleDevice.bleCharacteristicObservers.add(observer);
        }
    }

    @Override
    public void removeBLEReadObserver(String address, HPatchBLECharacteristicObserver observer) {
        BLEDevice bleDevice = addressBLEDeviceMap.get(address);
        if (bleDevice != null) {
            bleDevice.bleCharacteristicObservers.remove(observer);
        }
    }

    @Override
    public void addBLEHeartRateObserver(String address, HPatchBLEHeartRateObserver observer) {
        BLEDevice bleDevice = addressBLEDeviceMap.get(address);
        if (bleDevice != null) {
            synchronized (bleDevice.bleHeartRateObservers) {
                bleDevice.bleHeartRateObservers.add(observer);
            }
        }
    }

    @Override
    public void removeBLEHeartRateObserver(String address, HPatchBLEHeartRateObserver observer) {
        BLEDevice bleDevice = addressBLEDeviceMap.get(address);
        if (bleDevice != null) {
            synchronized (bleDevice.bleHeartRateObservers) {
                bleDevice.bleHeartRateObservers.remove(observer);
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                bluetoothLeService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String filename = sdf.format(date) + ".txt", rawdata;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String address = intent.getStringExtra(BluetoothLeService.ACTION_BLE_ADDRESS);
            final BLEDevice bleDevice = addressBLEDeviceMap.get(address);

            //Log.d(TAG, "GattUpdateReceiver: " + action);

            synchronized (mGattUpdateReceiver) {
                if (bleDevice != null) {
                    if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                        synchronized (bleDeviceObservers) {
                            for (BLEDeviceObserver observer : bleDeviceObservers) {
                                observer.onBLEDeviceConnected(bleDevice.hPatchDeviceBLEInfo);
                            }
                        }
                    } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                        synchronized (bleDeviceObservers) {
                            for (BLEDeviceObserver observer : bleDeviceObservers) {
                                observer.onBLEDeviceDisconnected(bleDevice.hPatchDeviceBLEInfo);
                            }
                        }
                    } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                        getGattServices(address, bluetoothLeService.getSupportedGattServices(address));
                    } else if (BluetoothLeService.ACTION_DATA_AVAILABLE_SPATCH_READ.equals(action)) {
                        byte[] data = intent.getByteArrayExtra(BluetoothLeService.ACTION_DATA_AVAILABLE_SPATCH_READ);
                        if (data != null) {
                            broadcastSPatchBLERead(bleDevice, data);
                        }
                    } else if (BluetoothLeService.ACTION_DATA_AVAILABLE_SPATCH_HEART_RATE_MEASUREMENT.equals(action)) {
                        try {
                            final int heartRateValue = Integer.parseInt(intent.getStringExtra(BluetoothLeService.MEASURED_HEART_RATE_VALUE));
                            //Log.d(TAG, "ACTION_DATA_AVAILABLE_SPATCH_HEART_RATE_MEASUREMENT: HR: " + heartRateValue);
                            synchronized (bleDevice.bleHeartRateObservers) {
                                for (HPatchBLEHeartRateObserver observer : bleDevice.bleHeartRateObservers) {
                                    observer.onHeartRateMeasurement(bleDevice.hPatchDeviceBLEInfo, heartRateValue);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    private class SPatchBLEReadData {
        BLEDevice bleDevice;
        byte[] data;

        SPatchBLEReadData(BLEDevice bleDevice, byte[] data) {
            this.bleDevice = bleDevice;
            this.data = data;
        }
    }

    private final ArrayList<SPatchBLEReadData> bleReadDataQueue = new ArrayList<>();

    private void broadcastSPatchBLERead(BLEDevice bleDevice, byte[] data) {
        if (!isLiveBLEReadThread) {
            new Thread(bleReadRunnable).start();
        }
        synchronized (bleReadDataQueue) {
            SPatchBLEReadData bleReadData = new SPatchBLEReadData(bleDevice, data);

            bleReadDataQueue.add(bleReadData);
            bleReadDataQueue.notifyAll();
        }
    }

    private boolean isLiveBLEReadThread;
    private final Runnable bleReadRunnable = new Runnable() {
        @Override
        public void run() {
            isLiveBLEReadThread = true;
            while (isLiveBLEReadThread) {
                SPatchBLEReadData bleReadData = null;

                try {
                    synchronized (bleReadDataQueue) {
                        bleReadDataQueue.wait(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (bleReadDataQueue) {
                    while (bleReadDataQueue.size() > 0) {
                        bleReadData = bleReadDataQueue.remove(0);

                        if (bleReadData != null) {
                            for (HPatchBLECharacteristicObserver observer : bleReadData.bleDevice.bleCharacteristicObservers) {
                                try {
                                    observer.onCharacteristicRead(bleReadData.bleDevice.hPatchDeviceBLEInfo, bleReadData.data);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    public void initialize() throws HPatchException
    {
        bleScanner = new BLEScanner(activity);

        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private boolean hasBLE() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private boolean isBLEEnabled() {
        boolean isBluetoothEnabled = false;

        if (hasBLE()) {
            if (blePermissionManager.isGranted()) {
                final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

                if (bluetoothAdapter != null) {
                    isBluetoothEnabled = bluetoothAdapter.isEnabled();
                }
            }
        }

        return isBluetoothEnabled;
    }

    private void enableBLE() {
        if (!isBLEEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, 1);
        }
    }

    @Override
    public void onBLEDeviceDetected(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
        synchronized (bleDeviceObservers) {
            for (BLEDeviceObserver observer : bleDeviceObservers) {
                observer.onBLEDeviceDetected(hPatchDeviceBLEInfo);
            }
        }
    }

    @Override
    public void onBLEDeviceConnected(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
        synchronized (bleDeviceObservers) {
            for (BLEDeviceObserver observer : bleDeviceObservers) {
                observer.onBLEDeviceConnected(hPatchDeviceBLEInfo);
            }
        }
    }

    @Override
    public void onBLEDeviceDisconnected(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
        synchronized (bleDeviceObservers) {
            for (BLEDeviceObserver observer : bleDeviceObservers) {
                observer.onBLEDeviceDisconnected(hPatchDeviceBLEInfo);
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_SPATCH_READ);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_SPATCH_WRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE_SPATCH_HEART_RATE_MEASUREMENT);
        return intentFilter;
    }

    private void getGattServices(String address, List<BluetoothGattService> gattServices) {
        final BLEDevice bleDevice = addressBLEDeviceMap.get(address);
        if (bleDevice == null || gattServices == null)
            return;
        for (BluetoothGattService gattService : gattServices) {
            //log(String.format("BluetoothGattService = %s", gattService.getUuid().toString()));

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                //log(String.format("BluetoothGattCharacteristic = %s", gattCharacteristic.getUuid().toString()));

                if(gattCharacteristic.getUuid().toString().equals(BleUuid.CHAR_SPATCH_WRITE)) {
                    bleDevice.mGattCharacteristicWrite = gattCharacteristic;
                } else if(gattCharacteristic.getUuid().toString().equals(BleUuid.CHAR_SPATCH_READ)) {
                    bleDevice.mGattCharacteristicRead = gattCharacteristic;
                    bluetoothLeService.setCharacteristicNotification(address, bleDevice.mGattCharacteristicRead, true);
                } else if(gattCharacteristic.getUuid().toString().equals(BleUuid.CHAR_SPATCH_FLOW_CTRL)) {
                    bleDevice.mGattCharacteristicFlowCtrl = gattCharacteristic;
                } else if(gattCharacteristic.getUuid().toString().equals(BleUuid.CHAR_HEART_RATE_MEASUREMENT)) {
                    bleDevice.mGattCharacteristicHeartRateMeasurement = gattCharacteristic;
                    bluetoothLeService.setCharacteristicNotification(address, gattCharacteristic, true);
                }
            }
        }
    }
}
