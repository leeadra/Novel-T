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
 * @file		HPatchPacketSource.java
 * @brief		HPatch Packet Source Enumeration
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/30
 *
 * <b>revision history :</b>
 * - 2016/12/30 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchdevice.HPatch3;

import java.util.HashMap;
import java.util.Map;

public enum HPatchPacketSource {
    ECG(0x00),  // Electrocardiogram
    PPG(0x01),  // Photoplethysmograph
    BIA(0x02),  // Bio Impedance Analysis
    GSR(0x03),  // Galvanic skin response
    EEG(0x04),  // Electroencephalogram

    SkinTemperature(0x10),

    Accelerometer(0x20),
    Gyroscope(0x21),

    None(0xFF)
    ;

    ///////////////////////////////
    // Methods for value setting

    private final int value;
    HPatchPacketSource(int value) {
        this.value = value;
    }
    public int getValue() { return value; }

    private static Map<Integer, HPatchPacketSource> map = new HashMap<>();
    static {
        for (HPatchPacketSource type : HPatchPacketSource.values()) {
            map.put(type.value, type);
        }
    }
    public static HPatchPacketSource valueOf(int type) {
        return map.get(type);
    }
}
