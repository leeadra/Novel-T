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
 * @file		HPatchBeatDetectDataContainer.java
 * @brief		Beat Detection Data Container
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/29
 *
 * <b>revision history :</b>
 * - 2016/11/29 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

public class HPatchBeatDetectDataContainer implements HPatchBeatDetectData {

    private int time;
    private float peakValue;

    public HPatchBeatDetectDataContainer(int time, float peakValue) {
        this.time = time;
        this.peakValue = peakValue;
    }

    @Override
    public int getTimeIndex() { return time; }

    @Override
    public float getPeakValue() { return peakValue; }
}
