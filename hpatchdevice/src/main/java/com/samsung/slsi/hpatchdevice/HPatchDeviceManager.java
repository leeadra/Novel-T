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
 * @file		HPatchDeviceManager.java
 * @brief		Detect HPatch Device through BLE
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/29
 *
 * <b>revision history :</b>
 * - 2016/11/29 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchdevice;

import com.samsung.slsi.BLEDeviceObserver;
import com.samsung.slsi.HPatchDetectObserver;
import com.samsung.slsi.HPatchDeviceBLEInfo;
import com.samsung.slsi.HPatchException;
import com.samsung.slsi.HPatchHostBLE;
import com.samsung.slsi.HPatchHostOS;
import com.samsung.slsi.HPatchManager;

import java.util.ArrayList;
import java.util.HashMap;

public class HPatchDeviceManager implements HPatchManager, BLEDeviceObserver {
    private HPatchHostOS hostOS;
    private HPatchHostBLE hPatchHostBLE;

    private final ArrayList<HPatchDetectObserver> hPatchDetectObservers = new ArrayList<>();
    private final HashMap<String, HPatchDevice> addressHPatchDeviceMap = new HashMap<>();

    public HPatchDeviceManager(HPatchHostOS hostOS, HPatchHostBLE hPatchHostBLE) {
        this.hostOS = hostOS;
        this.hPatchHostBLE = hPatchHostBLE;

        hPatchHostBLE.addBLEDeviceObserver(this);
    }

    public HPatchDevice get(String address) {
        if (addressHPatchDeviceMap.containsKey(address)) {
            return addressHPatchDeviceMap.get(address);
        } else {
            return null;
        }
    }

    public void cleanup() {
        hPatchHostBLE.removeBLEDeviceObserver(this);
    }

    @Override
    public void onBLEDeviceDetected(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
        HPatchDevice hPatchDevice;
        synchronized (addressHPatchDeviceMap) {
            hPatchDevice = addressHPatchDeviceMap.get(hPatchDeviceBLEInfo.address);
        }
        if (hPatchDevice == null) {
            hPatchDevice = HPatchDeviceFactory.getDevice(hPatchDeviceBLEInfo, hPatchHostBLE, hostOS);

            if (hPatchDevice != null) {
                synchronized (addressHPatchDeviceMap) {
                    addressHPatchDeviceMap.put(hPatchDeviceBLEInfo.address, hPatchDevice);
                }

                broadcastHPatchDeviceDetected(hPatchDevice);
            }
        } else {
            HPatchDeviceBLEInfo bleInfo = hPatchDevice.getBLEInfo();

            bleInfo.scanRecord = hPatchDeviceBLEInfo.scanRecord;
            bleInfo.rssi = hPatchDeviceBLEInfo.rssi;

            broadcastHPatchDeviceDetected(hPatchDevice);
        }
    }

    private void broadcastHPatchDeviceDetected(HPatchDevice hPatchDevice) {
        try {
            synchronized (hPatchDetectObservers) {
                for (HPatchDetectObserver observer : hPatchDetectObservers) {
                    observer.onHPatchDetected(hPatchDevice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBLEDeviceConnected(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
        HPatchDevice sPatchDevice;
        synchronized (addressHPatchDeviceMap) {
            sPatchDevice = addressHPatchDeviceMap.get(hPatchDeviceBLEInfo.address);
        }
        if (sPatchDevice != null) {
            sPatchDevice.onConnected();
        }
    }

    @Override
    public void onBLEDeviceDisconnected(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) {
        HPatchDevice sPatchDevice;
        synchronized (addressHPatchDeviceMap) {
            sPatchDevice = addressHPatchDeviceMap.get(hPatchDeviceBLEInfo.address);
            if (sPatchDevice != null) {
                addressHPatchDeviceMap.remove(hPatchDeviceBLEInfo.address);
            }
        }
        if (sPatchDevice != null) {
            sPatchDevice.onDisconnected();
        }
    }

    @Override
    public void addHPatchDeviceObserver(HPatchDetectObserver observer) {
        synchronized (hPatchDetectObservers) {
            hPatchDetectObservers.add(observer);
        }
    }

    @Override
    public void removeHPatchDeviceObserver(HPatchDetectObserver observer) {
        synchronized (hPatchDetectObservers) {
            hPatchDetectObservers.remove(observer);
        }
    }

    @Override
    public void startScanning() throws HPatchException {
        hPatchHostBLE.startBLEScanning();
    }

    @Override
    public void stopScanning() {
        hPatchHostBLE.stopBLEScanning();
    }
}
