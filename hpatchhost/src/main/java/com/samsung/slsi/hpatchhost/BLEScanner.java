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
 * @file		BLEScanner.java
 * @brief		BLE Scanner
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/23
 *
 * <b>revision history :</b>
 * - 2016/11/23 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchhost;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.samsung.slsi.HPatchException;

public class BLEScanner {
    private static final String TAG = BLEScanner.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private boolean isBLEScanning = false;

    public BLEScanner(Activity activity) throws HPatchException {
        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            throw new HPatchException("Unable to initialize BluetoothManager.");
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            throw new HPatchException("Unable to obtain a BluetoothAdapter.");
        }
    }

    public boolean isBLEScanning() {
        return isBLEScanning;
    }

    private BluetoothAdapter.LeScanCallback leScanCallback;
    public void startLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
        this.leScanCallback = leScanCallback;
        isBLEScanning = mBluetoothAdapter.startLeScan(leScanCallback);
    }

    public void stopLeScan() {
        isBLEScanning = false;
        mBluetoothAdapter.stopLeScan(leScanCallback);
    }
}
