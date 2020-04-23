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
 * @file		HeartRateVariability.java
 * @brief		Heart Rate Variablity Algorithm Wrapper
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

import android.os.Environment;

import com.samsung.slsi.FileLogUtil;
import com.samsung.slsi.HPatchAlgorithm;
import com.samsung.slsi.HPatchBeatDetect;
import com.samsung.slsi.HPatchBeatDetectData;
import com.samsung.slsi.HPatchValueContainer;
import com.samsung.slsi.TimeUtils;
import com.samsung.slsi.hpatchalgo.HPatchAlgorithmWrapper;

public class HeartRateVariability implements HPatchAlgorithm {

    private boolean isEnabled = true;
    private float samplesPerSecond = 250;

    public HeartRateVariability(float samplesPerSecond) {
        this.samplesPerSecond = samplesPerSecond;
    }

    @Override
    public String getName() {
        return "HRV";
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

    private boolean isFirst = true;
    private long lastUpdateTimeMS;
    private long last5MinMS;

    private FileLogUtil logUtil = new FileLogUtil(Environment.getExternalStorageDirectory() + "/MyHeartFit/LOG/");
    private String logFileName = "HRV_" + FileLogUtil.getDateTextForFile(System.currentTimeMillis()) + ".txt";

    @Override
    public void updateAlgorithmResult(HPatchBeatDetect hPatchBeatDetect, long targetTimeMS, HPatchValueContainer result) {
        final String[] resultItems = {
                "AVNN", "SDNN", "SDANN", "ASDNN",
                "NN50", "PNN50", "RMSSD", "TINN",
                "VLF", "LF", "HF"
        };

        if (isEnabled()) {
            final long updateIntervalMS = 1000 * 3; // 3 sec
            final long dataRange = 1000 * 60 * 5;   // 5 min

            if (lastUpdateTimeMS == 0) {
                lastUpdateTimeMS = targetTimeMS;
            } else {
                boolean isNeededUpdate;
                long interval = targetTimeMS - lastUpdateTimeMS;
                if (isFirst) {
                    isNeededUpdate = (interval > dataRange);
                } else {
                    isNeededUpdate = (interval > updateIntervalMS);
                }

                if (isNeededUpdate) {
                    lastUpdateTimeMS = targetTimeMS;

                    long firstTimeMS = targetTimeMS - dataRange;
                    HPatchBeatDetectData[] beatDetectData = hPatchBeatDetect.getBeatDetect(firstTimeMS, targetTimeMS);
                    if (beatDetectData != null) {
                        if (beatDetectData.length > 2) {
                            int n = beatDetectData.length;

                            int reset = (isFirst ? 1 : 0);
                            int[] qrsIndex = new int[n];
                            for (int i = 0; i < n; i++) {
                                qrsIndex[i] = beatDetectData[i].getTimeIndex();
                            }

                            boolean is5Min;
                            if (last5MinMS == 0) {
                                last5MinMS = targetTimeMS;
                                is5Min = true;
                            } else {
                                is5Min = ((targetTimeMS - last5MinMS) > 1000 * 60 * 5);
                                if (is5Min) {
                                    last5MinMS = targetTimeMS;
                                }
                            }

                            if (false && FileLogUtil.isInteralRelease && FileLogUtil.isStoreEnabled)  //ToDo: Only for debugging
                            {
                                String logText = "\n"
                                        + "//HRV: " + FileLogUtil.getDateText(targetTimeMS)
                                        + ", TT: " + TimeUtils.getTimeText(interval)
                                        + ", Reset: " + reset
                                        + ", QRS-Count: " + qrsIndex.length
                                        + ", FS: " + (int) samplesPerSecond
                                        + ", 5Min: " + is5Min
                                        + "\n";
                                System.out.println(logText);

                                logText += "int qrs_index_test_data[" + qrsIndex.length + "] = \n{";
                                for (int i = 0; i < qrsIndex.length; i++) {
                                    if (i % 16 == 0) {
                                        logText += "\n";
                                    }
                                    logText += qrsIndex[i] + ", ";
                                }
                                logText += "\n};\n";

                                //logUtil.logging(logFileName, logText);
                            }
//*
                            float[] results = HPatchAlgorithmWrapper.getECGHeartRateVariability(
                                    reset,
                                    qrsIndex,
                                    qrsIndex.length,
                                    (int) samplesPerSecond,
                                    (is5Min ? 1 : 0));
                            isFirst = false;

                            for (int i = 0; i < results.length && i < resultItems.length; i++) {
                                result.setValue(resultItems[i]).setValue(results[i]);
                            }

                            if (false && FileLogUtil.isInteralRelease && FileLogUtil.isStoreEnabled)  //ToDo: Only for debugging
                            {
                                String resultText = "";
                                for (int i = 0; i < results.length && i < resultItems.length; i++) {
                                    resultText += "float " + resultItems[i] + " = " + results[i] + ";\n";
                                }
                                //logUtil.logging(logFileName, resultText);
                            }
//*/
                        }
                    }
                }

                if (isFirst && !isNeededUpdate) {
                    String text = getProcessingText();
                    for (String itemName : resultItems) {
                        result.setValue(itemName).setValue(text);
                    }
                }
            }
        } else {
            String notAvailable = "N/A";
            String text = notAvailable;
            for (String itemName : resultItems) {
                result.setValue(itemName).setValue(text);
            }
        }
    }

    //private String progressText = "∙∙∙◦∙∙∙◦∙∙∙◦";
    //private String progressText = "●●●○●●●○●●●○";
    //private String progressText = "... ... ... ";
    //private String progressText = "∙∙∙ ∙∙∙ ∙∙∙ ";
    //private String progressText = "◦∙∙∙◦∙∙∙◦";
    private String progressText = " ∙∙∙∙ ∙∙∙∙ ∙∙∙∙ ";
    private int progressCharIndex;
    private String getProcessingText() {
        progressCharIndex += 4;
        if (progressCharIndex >= progressText.length()) {
            progressCharIndex = 0;
        }
        return progressText.substring(progressCharIndex, progressCharIndex + 4);
    }
}
