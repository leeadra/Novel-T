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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class HPatchExportEDF {
    private HPatchHostOS hostOS;
    private HPatchECGDataManager ecgDataManager;

    private int totalSampleCount;

    HPatchExportEDF(HPatchHostOS hostOS, HPatchECGDataManager ecgDataManager) {
        this.hostOS = hostOS;
        this.ecgDataManager = ecgDataManager;
    }

    private void fillText(byte[] buffer, int index, int limit, String text) {
        for (int i = 0; i < limit && i < text.length(); i++) {
            buffer[index + i] = (byte)(text.charAt(i));
        }
    }

    public void create(String path, String fileName, String recordName, float samplesPerSecond, float unitMilliVoltage) throws IOException {
        byte[] header = new byte[256 * 2];

        for (int i = 0; i < 256 * 2; i++) {
            header[i] = (byte)' ';
        }

        int index = 0;

        //Version of this data format
        header[index] = (byte)'0';
        index += 8;

        //Local patient identification
        recordName = fileName;
        fillText(header, index, 80, recordName);
        index += 80;

        //Local recording identification
        fillText(header, index, 80, getStartDateString(ecgDataManager.getFirstTimeMS()));
        index += 80;

        //Start date of recording (dd.mm.yy)
        fillText(header, index, 80, getDateText(ecgDataManager.getFirstTimeMS()));
        index += 8;

        //Start time of recording (hh.mm.ss)
        fillText(header, index, 80, getTimeText(ecgDataManager.getFirstTimeMS()));
        index += 8;

        //Number of bytes in header.
        fillText(header, index, 8, "" + (2 * 256));
        index += 8;

        //Reserved
        index += 44;

        double frames_per_second = samplesPerSecond;

        //Number of blocks (-1 if unknown)
        totalSampleCount = ecgDataManager.getTotalSampleCount();
        long frames_per_block = (long)(10 * frames_per_second + 0.5);
        long nblocks = totalSampleCount / frames_per_block + 1;
        fillText(header, index, 8, "" + nblocks);
        index += 8;

        //Duration of a block, in seconds.
        double seconds_per_block = frames_per_block / frames_per_second;
        fillText(header, index, 8, "10");
        index += 8;

        //Number of signals.
        fillText(header, index, 4, "" + 1);
        index += 4;

        //Label (e.g., EEG FpgCz or Body temp).
        String desc = "record " + recordName + ", signal 0";
        fillText(header, index, 16, "" + desc);
        index += 16;

        //Transducer type (e.g., AgAgCl electrode).
        String transducer = "transducer type not recorded";
        fillText(header, index, 80, transducer);
        index += 80;

        //Physical dimension (e.g., uV or degreeC).
        fillText(header, index, 8, "mV");
        index += 8;

        //Physical minimum (e.g., -500 or 34).
        double pmin = -146.942;
        fillText(header, index, 8, "" + pmin);
        index += 8;

        //Physical maximum (e.g., 500 or 40).
        double pmax = 146.937;
        fillText(header, index, 8, "" + pmax);
        index += 8;

        //Digital minimum (e.g., -2048).
        int dmin = -32768;
        fillText(header, index, 8, "" + dmin);
        index += 8;

        //Digital maximum (e.g., 2047).
        int dmax = 32767;
        fillText(header, index, 8, "" + dmax);
        index += 8;

        //Prefiltering (e.g., HP:0.1Hz LP:75Hz).
        String prefiltering = "prefiltering not recorded";
        fillText(header, index, 80, prefiltering);
        index += 80;

        //Number of samples per block.
        fillText(header, index, 8, "" + frames_per_block);
        index += 8;

        //(The last 32*nsig bytes in the header are unused.)

        //hostOS.storeFile(path, fileName, header, false);

        //Write the data blocks.
        {
            int i = 0;
            int unitSize = (int)(frames_per_block);

            for (int n = 1; n <= nblocks; n++) {
                int[] samples = ecgDataManager.getSamples(i, i + unitSize);
                ByteBuffer byteBuffer = ByteBuffer.allocate(samples.length * 2);
                for (int sample : samples) {
                    byteBuffer.put((byte) (sample & 0xFF));
                    byteBuffer.put((byte) ((sample >> 8) & 0xFF));
                }
                //hostOS.storeFile(path, fileName, byteBuffer.array(), true);

                i += unitSize;
            }
        }
    }

    private static final String[] MonthName = new String[] {
            "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };

    private String getStartDateString(long t) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(t);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        return String.format(Locale.getDefault(),
                "Startdate %02d-%s-%04d",
                day,
                MonthName[month],
                year);
    }

    private String getTimeText(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh.mm.ss");
        return sdf.format(new Date(t));
    }

    private String getDateText(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
        return sdf.format(new Date(t));
    }
}
