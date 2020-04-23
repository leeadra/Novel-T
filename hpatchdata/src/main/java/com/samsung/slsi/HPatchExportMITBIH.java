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
 * @file		HPatchExportMITBIH.java
 * @brief		HPatch Export as MIT BIH format
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/22
 *
 * <b>revision history :</b>
 * - 2016/12/22 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

class HPatchExportMITBIH {
    private HPatchHostOS hostOS;
    private HPatchECGDataManager ecgDataManager;

    private int totalSampleCount;

    HPatchExportMITBIH(HPatchHostOS hostOS, HPatchECGDataManager ecgDataManager) {
        this.hostOS = hostOS;
        this.ecgDataManager = ecgDataManager;
    }

    void createHEA(String path, String fileName, String recordName, float samplesPerSecond, float unitMilliVoltage) throws IOException {
        String heaFileName = fileName + ".hea";

        String hea = "";

        totalSampleCount = ecgDataManager.getTotalSampleCount();

        //https://www.physionet.org/physiotools/wag/header-5.htm

        //Record line
        hea += (fileName); // name
        hea += (" ");
        hea += (1);  //number of signals
        hea += (" ");
        hea += (samplesPerSecond); //sampling frequency (in samples per second per signal)
        hea += (" ");
        hea += (totalSampleCount); //number of samples per signal
        hea += (" ");
        hea += (getTimeText(ecgDataManager.getFirstTimeMS()));  //base time
        hea += (" ");
        hea += (getDateText(ecgDataManager.getFirstTimeMS()));  //base date
        hea += ("\r\n");

        //Signal specification lines
        hea += (fileName + ".dat"); //file name
        hea += (" ");
        hea += (16); //format
        hea += (" ");
        hea += (unitMilliVoltage);   //ADC gain (ADC units per physical unit)
        hea += (" ");
        hea += ("S-PATCH");
        hea += ("\r\n");

        //hostOS.storeFile(path, heaFileName, hea.toString().getBytes(), false);
    }

    private String getTimeText(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(t));
    }

    private String getDateText(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new Date(t));
    }

    void createDat(String path, String fileName) throws IOException {
        String datFileName = fileName + ".dat";

        final int unitSize = 1024;
        int totalCount = totalSampleCount;
        int index = 0;
        boolean isAppend = false;

        while (index < totalCount) {
            int count = (totalCount - index);
            if (count > unitSize) {
                count = unitSize;
            }

            int[] samples = ecgDataManager.getSamples(index, index + count);
            ByteBuffer byteBuffer = ByteBuffer.allocate(samples.length * 2);
            for (int sample : samples) {
                byteBuffer.put((byte)(sample & 0xFF));
                byteBuffer.put((byte)((sample >> 8) & 0xFF));
            }
            //hostOS.storeFile(path, datFileName, byteBuffer.array(), isAppend);
            isAppend = true;

            index += count;
        }
    }
}
