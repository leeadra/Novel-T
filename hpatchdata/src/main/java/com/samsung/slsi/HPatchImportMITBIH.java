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
 * @file		HPatchImportMITBIH.java
 * @brief		HPatch Import as MIT BIH format
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/24
 *
 * <b>revision history :</b>
 * - 2016/12/24 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class HPatchImportMITBIH {

    private HPatchHostOS hostOS;

    private String recordName;
    private int numberOfSignals;
    private float samplesPerSecond;
    private int numberOfSamples;
    private long startTimeMS;

    private String path;
    private String datFileName;
    private int signalFormat;
    private float unitMilliVoltage;


    HPatchImportMITBIH(HPatchHostOS hostOS) {
        this.hostOS = hostOS;
    }

    int getNumberOfSamples() {
        return numberOfSamples;
    }

    HPatchDataManager importMITBIH(String path, String fileName) throws IOException {
        this.path = path;

        String heaFileName;
        {
            String lowerFileName = fileName.toLowerCase();
            if (lowerFileName.length() > 4) {
                String extension = lowerFileName.substring(lowerFileName.length() - 4, lowerFileName.length());
                if (extension.contains(".hea")) {
                    heaFileName = fileName;
                } else if (extension.contains(".sig") || extension.contains(".dat")) {
                    heaFileName = fileName.substring(0, fileName.length() - 4) + ".hea";
                } else {
                    throw new IOException("Invalid file");
                }
            } else {
                throw new IOException("Invalid file");
            }
        }
        //readHea(path, heaFileName);
        return readDat(path, datFileName);
    }

    private void readHea(String path, String fileName) throws IOException {
        byte[] bytes = hostOS.restoreFile(path, fileName, 0, 0);
        String text = new String(bytes);

        String[] textLines = text.split("[\n]");

        if (textLines.length <= 1) {
            //Invalid
            throw new IOException("Invalid Format");
        } else {
            String firstLine = textLines[0];

            String[] tokens = firstLine.split("[ ]+");

            if (tokens.length < 4) {
                //Invalid
                throw new IOException("Invalid Format");
            } else {
                //Record line
                recordName = tokens[0];
                numberOfSignals = Integer.parseInt(tokens[1]);
                samplesPerSecond = Float.parseFloat(tokens[2]);
                numberOfSamples = Integer.parseInt(tokens[3]);
                if (tokens.length > 5) {
                    String baseTime = tokens[4];
                    String baseDate = tokens[5];

                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

                        Date date = simpleDateFormat.parse(baseTime + " " + baseDate);
                        startTimeMS = date.getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                //Parse Only the First Signal
                String signalInformation = textLines[1];
                tokens = signalInformation.split("[ ]+");
                if (tokens.length < 3) {
                    //Invalid
                    throw new IOException("Invalid Format");
                } else {
                    datFileName = tokens[0];
                    signalFormat = Integer.parseInt(tokens[1]);
                    if (signalFormat != 16) {
                        throw new IOException("Only 16 Format is Supported");
                    }
                    if (tokens[2].contains("/")) {
                        unitMilliVoltage = Float.parseFloat(tokens[2].substring(0, tokens[2].indexOf('/')));
                    } else {
                        unitMilliVoltage = Float.parseFloat(tokens[2]);
                    }
                }
            }
        }
    }

    private static SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm:ss dd/mm/yyyy");

    private HPatchDataManager readDat(String path, String fileName) throws IOException {
        int id;
        try {
            id = Integer.parseInt(recordName);
        } catch (Exception e) {
            id = 0;
        }

        HPatchECGSignal ecgSignal = new HPatchECGSignal(hostOS, (int)(60 * samplesPerSecond));
        ecgSignal.restore(path, fileName);

        hPatch = new FileHPatch(id, unitMilliVoltage, samplesPerSecond);
        HPatchData hPatchData = new HPatchData(hostOS, path, id, unitMilliVoltage, samplesPerSecond, samplesPerSecond, ecgSignal);
        dataManager = new HPatch3DataManager(
                hostOS,
                hPatch,
                hostOS.getTemporaryPath(),
                hPatchData);

        dataManager.setFirstTime(startTimeMS);

        //Todo: Only 16 format support, other cases need converting
//        new Thread(datLoadRunnable).start();
        return dataManager;
    }

    private HPatch hPatch;
    private HPatch3DataManager dataManager;

    private Runnable datLoadRunnable = new Runnable() {
        @Override
        public void run() {
            final int unitSize = 1024 * 4;
            int sequence = 0;

            int index = 0;

            boolean isEOF = false;
            while (!isEOF) {
                byte[] data = null;
                try {
                    data = hostOS.restoreFile(path, datFileName, index, unitSize);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                index += data.length;
                isEOF = data.length < unitSize;

                int count = data.length / 2;
                int[] ecgSignalData = new int[count];

                ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                for (int i = 0; i < count; i++) {
                    int signalValue;

                    signalValue = (byteBuffer.get() & 0xFF);
                    signalValue += (byteBuffer.get() & 0xFF) * 256;

                    ecgSignalData[i] = signalValue;
                }

                if (sequence == 0) {
                    dataManager.setFirstTime(startTimeMS);
                }
                dataManager.onUpdateECG(hPatch, sequence, ecgSignalData);
                sequence++;
            }
        }
    };
}
