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
 * @file		HPatchOTA.java
 * @brief		HPatch Device On-The-Air (OTA) Interface
 *              BLE FW Update
 *              BP FW Update
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2017/05/16
 *
 * <b>revision history :</b>
 * - 2017/05/16 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

public interface HPatchOTA {
    OTA getBLE();
    OTA getBP();
}
