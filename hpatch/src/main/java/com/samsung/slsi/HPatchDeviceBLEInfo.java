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
 * @file		HPatchDeviceBLEInfo.java
 * @brief		HPatch Device BLE Information
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

public class HPatchDeviceBLEInfo {
    public String name;
    public String address;

    public byte[] scanRecord;

    public int rssi;

    public HPatchDeviceBLEInfo(String name, String address, byte[] scanRecord, int rssi) {
        this.name = name;
        this.address = address;

        this.scanRecord = scanRecord;

        this.rssi = rssi;
    }
}
