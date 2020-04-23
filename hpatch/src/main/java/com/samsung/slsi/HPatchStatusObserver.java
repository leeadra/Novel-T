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
 * @file		HPatchStatusObserver.java
 * @brief		HPatch Status Observer interface
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

public interface HPatchStatusObserver {
    void updateSPatchDeviceInformation(HPatch hPatch);
    void updateBatteryRatio(HPatch hPatch, int batteryRatio);
    void updateBLEConnectionStatus(HPatch hPatch, boolean isBLEConnected);
    void updateLeadContactStatus(HPatch hPatch, int leadStatus);

    void onError(HPatch hPatch, HPatchError id, String message);
}
