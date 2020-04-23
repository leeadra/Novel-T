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
 * @file		HeartRateEstimation.java
 * @brief		Heart Rate Estimation Algorithm Wrapper
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

package com.samsung.slsi.hpatchalgorithm;

import com.samsung.slsi.HPatchAlgorithm;
import com.samsung.slsi.HPatchBeatDetect;
import com.samsung.slsi.HPatchBeatDetectData;
import com.samsung.slsi.HPatchValueContainer;
import com.samsung.slsi.hpatchalgo.HPatchAlgorithmWrapper;

public class HeartRateEstimation implements HPatchAlgorithm {

    private static final int MAX_HEART_RATE = 300;
    private static final int MIN_HEART_RATE = 20;

    private boolean isEnabled = true;
    private float samplesPerSecond = 256;

    public HeartRateEstimation(float samplesPerSecond) {
        this.samplesPerSecond = samplesPerSecond;
    }

    @Override
    public String getName() {
        return "HR";
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void enable(boolean isEnabled) {
        if (!this.isEnabled && isEnabled) {
            isFirst = true;
            lastUpdateTimeMS = 0;
        }
        this.isEnabled = isEnabled;
    }

    private int maxHR = Integer.MIN_VALUE;
    private int minHR = Integer.MAX_VALUE;

    private boolean isFirst = true;
    private long lastUpdateTimeMS;

    @Override
    public void updateAlgorithmResult(HPatchBeatDetect hPatchBeatDetect, long targetTimeMS, HPatchValueContainer result) {
        if (isEnabled) {
            final long updateIntervalMS = 2000;
            final long dataRange = 1000 * 4;

            if (lastUpdateTimeMS == 0) {
                lastUpdateTimeMS = targetTimeMS;
                isFirst = true;
            } else {
                boolean isNeededUpdate;
                long interval = targetTimeMS - lastUpdateTimeMS;
                if (interval < 0) {
                    lastUpdateTimeMS = targetTimeMS;
                    isFirst = true;
                }
                if (isFirst) {
                    isNeededUpdate = (interval > dataRange);
                } else {
                    isNeededUpdate = (interval > updateIntervalMS);
                }

                //log("HR: Need Update? " + isNeededUpdate + " Interval: " + interval);

                if (isNeededUpdate) {
                    long firstTimeMS = targetTimeMS - dataRange;
                    HPatchBeatDetectData[] beatDetectData = hPatchBeatDetect.getBeatDetect(firstTimeMS, targetTimeMS);
                    if (beatDetectData != null) {
                        if (beatDetectData.length > 0) {
                            int n = beatDetectData.length;

                            int reset = (isFirst ? 1 : 0);
                            int[] qrsIndex = new int[n];
                            for (int i = 0; i < n; i++) {
                                qrsIndex[i] = beatDetectData[i].getTimeIndex();
                            }
//*
                            /*log("HR: " + targetTimeMS + ":" + reset
                                    + ", QRS: " + qrsIndex.length
                                    + ", FS: " + (int) samplesPerSecond
                            );*/
//*/
                            int hr;
                            hr = HPatchAlgorithmWrapper.getECGHeartRate(reset, qrsIndex, qrsIndex.length, (int) samplesPerSecond);
                            if (hr > 0) {
                                isFirst = false;

                                if (hr > MAX_HEART_RATE) {
                                    hr = MAX_HEART_RATE;
                                } else if (hr < MIN_HEART_RATE) {
                                    hr = MIN_HEART_RATE;
                                }

                                lastUpdateTimeMS = targetTimeMS;


                                result.setValue("HeartRate").setValue(hr);

                                if (maxHR < hr) {
                                    maxHR = hr;
                                }
                                result.setValue("HeartRateMax").setValue(maxHR);

                                if (minHR > hr) {
                                    minHR = hr;
                                }
                                result.setValue("HeartRateMin").setValue(minHR);
                            }
                        } else {
                            //log("HR: " + targetTimeMS + ":: " + beatDetectData.length);
                        }
                    }
                }
            }
        } else {
            String notAvailable = "N/A";
            result.setValue("HeartRate").setValue(notAvailable);
            result.setValue("HeartRateMax").setValue(notAvailable);
            result.setValue("HeartRateMin").setValue(notAvailable);
        }
    }
}