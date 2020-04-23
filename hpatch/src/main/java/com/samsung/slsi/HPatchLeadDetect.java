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
 * @file		HPatchLeadDetect.java
 * @brief		HPatch Lead Detect interface
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2017/1/4
 *
 * <b>revision history :</b>
 * - 2017/1/4 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

public interface HPatchLeadDetect {
    Boolean isLeadOn();

    float getThreshold();
    void setThreshold(float theshold);
}
