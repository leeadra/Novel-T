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
 * @file		BleUuid.java
 * @brief		BLE UUID
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/23
 *
 * <b>revision history :</b>
 * - 2016/11/23 First creation (from old-S-Patch App code)
 *******************************************************************************
 */

package com.samsung.slsi.hpatchhost;

public class BleUuid {
    public static final String SERVICE_SPATCH = "0783b03e-8535-b5a0-7140-a304d2495cb7";
    public static final String CHAR_SPATCH_READ = "0783b03e-8535-b5a0-7140-a304d2495cb8";
    public static final String CHAR_SPATCH_WRITE = "0783b03e-8535-b5a0-7140-a304d2495cba";
    public static final String CHAR_SPATCH_FLOW_CTRL = "0783b03e-8535-b5a0-7140-a304d2495cb9";

    public static final String SERVICE_HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb"; //HRS
    public static final String CHAR_HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BODY_SENSOR_LOCATION = "00002a38-0000-1000-8000-00805f9b34fb";

    public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
}