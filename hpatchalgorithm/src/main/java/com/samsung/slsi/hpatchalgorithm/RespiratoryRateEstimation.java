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
 * @file		RespiratoryRateEstimation.java
 * @brief		Respiratory Rate Estimation Algorithm Wrapper
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
import com.samsung.slsi.hpatchalgo.HPatchAlgorithmWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

public class RespiratoryRateEstimation implements HPatchAlgorithm {

    private static String loggingFolderName;
    private static boolean isRRELoggingEnabled = false;
    public static void enableLogging(boolean isEnabled, String folderName) {
        loggingFolderName = folderName;
        isRRELoggingEnabled = isEnabled;
    }

    private boolean isEnabled = true;

    public RespiratoryRateEstimation() {
    }

    @Override
    public String getName() {
        return "RespirationRate";
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void enable(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    private boolean isFirst = true;
    private long lastUpdateTimeMS;

    private FileLogUtil logUtil = new FileLogUtil(Environment.getExternalStorageDirectory() + "/MyHeartFit/LOG/");
    private String logFileName = "RR_" + FileLogUtil.getDateTextForFile(System.currentTimeMillis()) + ".txt";

    @Override
    public void updateAlgorithmResult(HPatchBeatDetect hPatchBeatDetect, long targetTimeMS, HPatchValueContainer result) {
        String resultText = null;

        if (isEnabled()) {
            final int secWin = 64;

            final long updateIntervalMS = 3000;
            final long dataRange = 1000 * secWin;

            if (lastUpdateTimeMS == 0) {
                lastUpdateTimeMS = targetTimeMS;
                isFirst = true;
            } else {
                boolean isNeededUpdate;
                long interval = targetTimeMS - lastUpdateTimeMS;
                if (isFirst) {
                    isNeededUpdate = (interval > dataRange);
                } else {
                    isNeededUpdate = (interval > updateIntervalMS);
                }

                if (isNeededUpdate) {
                    long firstTimeMS = targetTimeMS - dataRange;
                    HPatchBeatDetectData[] beatDetectData = hPatchBeatDetect.getBeatDetect(firstTimeMS, targetTimeMS);
                    if (beatDetectData != null) {
                        if (beatDetectData.length > 2) {
                            if (false) {
                                String logText = "//RRE: "
                                        + firstTimeMS + " ~ " + targetTimeMS
                                        + "(" + (targetTimeMS - firstTimeMS) + ")"
                                        + ", secWin: " + dataRange
                                        + "\n";
                                System.out.println(logText);
                            }

                            float respiratoryRateEstimation = getLastestRespirationRateEstimation(
                                    beatDetectData,
                                    secWin);
                            isFirst = false;

                            if (respiratoryRateEstimation > 0) {
                                resultText = String.format(Locale.getDefault(), "%.0f", respiratoryRateEstimation);
                            }
                        }
                    }
                } else if (isFirst) {
                    resultText = getProcessingText();
                }
            }
        } else {
            resultText = "N/A";
        }

        if (resultText != null) {
            result.setValue(getName()).setValue(resultText);
        }
    }

    private float getLastestRespirationRateEstimation(HPatchBeatDetectData[] beatDetectData, int secWin) {
        float respiratoryRateEstimation = 0;

        int count = beatDetectData.length;
        if (count > 10) {
            int[] Ridx;
            float[] QRSminmax;
            int num_data;

            Ridx = new int[count];
            QRSminmax = new float[count];
            num_data = count;

            String logText = "//" + FileLogUtil.getDateText(System.currentTimeMillis()) + "\n";
            logText += "//Count: " + count + ", secWin: " + secWin + "\n";

            String RidxText = "int Ridx[" + count + "] = {";
            String QRSminmaxText = "float QRSminmax[" + count + "] = {";

            for (int i = 0; i < count; i++) {
                Ridx[i] = beatDetectData[i].getTimeIndex() - beatDetectData[0].getTimeIndex();
                QRSminmax[i] = beatDetectData[i].getPeakValue();

                if(i % 16 == 0) {
                    RidxText += "\n";
                    QRSminmaxText += "\n";
                }

                RidxText += Ridx[i] + ", ";
                QRSminmaxText += QRSminmax[i] + "f, ";
            }

            RidxText += "\n};\n";
            QRSminmaxText += "\n};\n";

            logText += RidxText;
            logText += QRSminmaxText;
            logText += "\n\n";

            //logUtil.logging(logFileName, logText);

            //System.out.println("RREst: RR: Index: " + Ridx[0] + " ~ " + Ridx[count - 1] + ", Count: " + count);
            respiratoryRateEstimation = HPatchAlgorithmWrapper.getRespirationRateEstimation(Ridx, QRSminmax, num_data, secWin);

            //logUtil.logging(logFileName, "//Result RR: " + respiratoryRateEstimation + "\n\n");

            //System.out.println("RR: Index: " + Ridx[0] + " ~ " + Ridx[count - 1] + ", Count: " + count + ", RR: " + respiratoryRateEstimation);
            /*if (isRRELoggingEnabled) {
                storeBeatDetectTestCase(Ridx, QRSminmax, num_data, secWin, respiratoryRateEstimation);
            }*/
        }

        return respiratoryRateEstimation;
    }

    private int index;
    private void storeBeatDetectTestCase(int[] Ridx, float[] QRSminmax, int num_data, int secWin, float respiratoryRate) {
        String path = Environment.getExternalStorageDirectory().toString();
        if (loggingFolderName != null) {
            if (loggingFolderName.length() > 0) {
                path += "/" + loggingFolderName;
            }
        }
        path += "/RRE_test/";

        String fileName = "RR_" + index + ".txt";
        index++;

        try {
            File dir = makeDirectory(path);
            File file = makeFile(dir, path + fileName);

            FileOutputStream fileOutputStream;

            fileOutputStream = new FileOutputStream(file);
            PrintStream printer = new PrintStream(fileOutputStream);
            printer.println("" + num_data + "\t" + secWin + "\t" + respiratoryRate);

            for (int i = 0; i < num_data; i++) {
                printer.println("" + Ridx[i] + "\t" + QRSminmax[i]);
            }

            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private File makeDirectory(String dirPath) throws FileNotFoundException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new FileNotFoundException(dirPath);
            }
        }
        return dir;
    }

    private File makeFile(File dir , String filePath) throws IOException {
        File file = null;
        if (dir.isDirectory()) {
            file = new File(filePath);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException(filePath);
                }
            }
        }
        return file;
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
