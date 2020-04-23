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
 * @file		HPatchUtil.java
 * @brief		Detect HPatch Device by ScanRecord of BLE
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/22
 *
 * <b>revision history :</b>
 * - 2016/11/22 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchdevice;

public class HPatchUtil {
    public static int getMajor(byte[] scanRecord) {
        int major = 0;
        if (scanRecord != null) {
            if (scanRecord.length > 26) {
                major = ((scanRecord[25] << 8) & 0x0000ff00) + (scanRecord[26] & 0x000000ff);
            }
        }
        return major;
    }

    public static int getMinor(byte[] scanRecord) {
        int minor = 0;
        if (scanRecord != null) {
            if (scanRecord.length > 28) {
                minor = ((scanRecord[27] << 8) & 0x0000ff00) + (scanRecord[28] & 0x000000ff);
            }
        }
        return minor;
    }

    public static boolean isNewProtocol(byte[] scanRecord) {
        boolean isNewProtocol = false;
        if(scanRecord != null) {
            if (scanRecord.length > 59) {
                isNewProtocol = (scanRecord[58] == (byte) 0xff) && (scanRecord[59] == (byte) 0xff);
            }
        }
        return isNewProtocol;
    }

    public static int getBLEFWVersion(byte[] scanRecord) {
        int major = 0;
        int middle = 0;
        int minor = 0;
        if (scanRecord != null) {
            if (scanRecord.length > 59) {
                major = (scanRecord[57] & 0xff);
                middle = (scanRecord[58] & 0xff);
                minor = (scanRecord[59] & 0xff);
            }
        }
        return major * 0x10000 + middle * 0x100 + minor;
    }
}
