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
 * @file		HPatchBLEException.java
 * @brief		HPatch BLE Exception
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2016/11/22
 *
 * <b>revision history :</b>
 * - 2016/11/22 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

public class HPatchBLEException extends HPatchException {
    private HPatchDeviceBLEInfo hPatchDeviceBLEInfo;

    public HPatchBLEException(HPatchDeviceBLEInfo hPatchDeviceBLEInfo, String message) {
        super(message);

        this.hPatchDeviceBLEInfo = hPatchDeviceBLEInfo;
    }

    public HPatchDeviceBLEInfo getBLEInfo() {
        return hPatchDeviceBLEInfo;
    }
}
