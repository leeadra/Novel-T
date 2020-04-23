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
 * @file		HPatchBeatDetect.java
 * @brief		Beat-Detect Data Container Access Interface
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

import java.io.IOException;

public interface HPatchBeatDetect {
    long getBeatDetectFirstTimeMS();
    long getBeatDetectLastTimeMS();

    /**
     * This method returns raw ECG signal values[firstTimeIndex to lastTimeIndex].
     * @param firstTimeMS the first time millisecond
     * @param lastTimeMS  the last time millisecond
     * @return raw ECG signal values
     */
    HPatchBeatDetectData[] getBeatDetect(long firstTimeMS, long lastTimeMS);

    void add(int[] time, float[] peakValue) throws IOException;
}
