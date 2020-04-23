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
 * @file		HPatchHostBLE.java
 * @brief		HPatch Host Device's BLE interface
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2016/11/21
 *
 * <b>revision history :</b>
 * - 2016/11/21 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

public interface HPatchHostBLE {
    boolean isBLEScanning();
    void startBLEScanning() throws HPatchException;
    void stopBLEScanning();

    void addBLEDeviceObserver(BLEDeviceObserver observer);
    void removeBLEDeviceObserver(BLEDeviceObserver observer);

    void connectBLE(HPatchDeviceBLEInfo hPatchDeviceBLEInfo) throws HPatchBLEException;
    void disconnectBLE(HPatchDeviceBLEInfo hPatchDeviceBLEInfo);

    void write(String address, byte[] packet) throws HPatchBLEException;

    void addBLEReadObserver(String address, HPatchBLECharacteristicObserver observer);
    void removeBLEReadObserver(String address, HPatchBLECharacteristicObserver observer);

    void addBLEHeartRateObserver(String address, HPatchBLEHeartRateObserver observer);
    void removeBLEHeartRateObserver(String address, HPatchBLEHeartRateObserver observer);
}
