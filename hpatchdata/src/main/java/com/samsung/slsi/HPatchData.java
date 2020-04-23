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
 * @file		HPatchData.java
 * @brief		HPatch Data Handler
 *              Store streaming data and Cache for access
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/29
 *
 * <b>revision history :</b>
 * - 2016/12/29 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class HPatchData {

    private static final int ECG_STREAMING_BUFFER_LIMIT_SECONDS = 60 * 2;
    private static final int BeatDetectLimitSize = 5 * 4 * 60;

    private static final byte[] FileMarker = { 0x53, 0x50, 0x49, 0x46 };  // SPIF : S-PATCH Information
    private final HPatchHostOS hostOS;

    private String path;
    private String fileName;

    private static final int Version = 0x00000001;

    private int id;

    private long ecgStartTimeMS;

    private float unitMilliVoltage;
    private float ecgDeviceSamplesPerSecond;
    private float ecgTransferSamplesPerSecond;

    private String subFolderName;
    private String eventDefineFileName;
    private String eventDataFileName;
    private String ecgSignalFileName;
    private String beatDetectFileName;

    private HPatchEventDefine eventDefine;
    private HPatchEventData eventData;
    private HPatchECGSignal ecgSignal;
    private HPatchBeatDetectContainer beatDetects;


    public HPatchData(HPatchHostOS hostOS, String path, int id, float unitMilliVoltage, float ecgDeviceSamplesPerSecond, float ecgTransferSamplesPerSecond) throws IOException {
        this.hostOS = hostOS;

        this.id = id;

        this.unitMilliVoltage = unitMilliVoltage;
        this.ecgDeviceSamplesPerSecond = ecgDeviceSamplesPerSecond;
        this.ecgTransferSamplesPerSecond = ecgTransferSamplesPerSecond;

        if (path != null) {
            String idTimeText = String.format(Locale.getDefault(), "%07d ", id) + getDateText(System.currentTimeMillis());
            fileName = idTimeText + ".sp";
            subFolderName = idTimeText + "/";

            eventDefineFileName = idTimeText + ".sed";
            eventDataFileName = idTimeText + ".sev";
            ecgSignalFileName = idTimeText + ".sig";
            beatDetectFileName = idTimeText + ".sbd";

            create(path);
        }
    }

    public HPatchData(HPatchHostOS hostOS, String path, int id, float unitMilliVoltage, float ecgDeviceSamplesPerSecond, float ecgTransferSamplesPerSecond, HPatchECGSignal ecgSignal) throws IOException {
        this.hostOS = hostOS;

        this.id = id;

        this.unitMilliVoltage = unitMilliVoltage;
        this.ecgDeviceSamplesPerSecond = ecgDeviceSamplesPerSecond;
        this.ecgTransferSamplesPerSecond = ecgTransferSamplesPerSecond;

        this.ecgSignal = ecgSignal;
    }

    private void create(String path) throws IOException {
        //store(path, fileName);

        eventDefine = new HPatchEventDefine(hostOS, path + "/" + getSubFolderName(), getEventDefineFileName());
        eventData = new HPatchEventData(hostOS, path + "/" + getSubFolderName(), getEventDataFileName());

        int ecgStreamingBufferLimit = (int)(ecgTransferSamplesPerSecond * ECG_STREAMING_BUFFER_LIMIT_SECONDS);
        ecgSignal = new HPatchECGSignal(hostOS, ecgStreamingBufferLimit);
        //ecgSignal.create(path + "/" + getSubFolderName(), getEcgSignalFileName());

        beatDetects = new HPatchBeatDetectContainer(hostOS, ecgTransferSamplesPerSecond, BeatDetectLimitSize);
        //beatDetects.create(path + "/" + getSubFolderName(), getBeatDetectFileName());
    }

    private void recreate(String path) throws IOException {
        eventDefine = new HPatchEventDefine(hostOS, path + "/" + getSubFolderName(), getEventDefineFileName());
        /*try {
            eventDefine.restore();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        eventData = new HPatchEventData(hostOS, path + "/" + getSubFolderName(), getEventDataFileName());
        /*try {
            eventData.restore();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        int ecgStreamingBufferLimit = (int)(ecgTransferSamplesPerSecond * ECG_STREAMING_BUFFER_LIMIT_SECONDS);
        ecgSignal = new HPatchECGSignal(hostOS, ecgStreamingBufferLimit);
        //ecgSignal.restore(path + "/" + getSubFolderName(), getEcgSignalFileName());

        beatDetects = new HPatchBeatDetectContainer(hostOS, ecgTransferSamplesPerSecond, BeatDetectLimitSize);
        //beatDetects.create(path + "/" + getSubFolderName(), getBeatDetectFileName());
    }

    public void clear() throws InterruptedException {
        if (ecgSignal != null) {
            ecgSignal.clear();
            ecgSignal = null;
        }

        if (beatDetects != null) {
            beatDetects.clear();
            beatDetects = null;
        }
    }

    public String getFileName() {
        return fileName;
    }


    public int getVersion() {
        return Version;
    }


    public int getId() {
        return id;
    }

    public long getECGStartTime() {
        return ecgStartTimeMS;
    }

    public void setECGStartTime(long time) {
        ecgStartTimeMS = time;
        try {
            store(path, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float getUnitMilliVoltage() {
        return unitMilliVoltage;
    }

    public float getEcgTransferSamplesPerSecond() {
        return ecgTransferSamplesPerSecond;
    }


    public String getSubFolderName() {
        return subFolderName;
    }

    public String getEventDefineFileName() {
        return eventDefineFileName;
    }

    public String getEventDataFileName() {
        return eventDataFileName;
    }

    public String getEcgSignalFileName() {
        return ecgSignalFileName;
    }

    public String getBeatDetectFileName() {
        return beatDetectFileName;
    }


    public HPatchEventDefine getEventDefine() {
        return eventDefine;
    }

    public HPatchEventData getEventData() {
        return eventData;
    }

    public HPatchECGSignal getECGSignal() {
        return ecgSignal;
    }

    public HPatchBeatDetect getBeatDetect() {
        return beatDetects;
    }

    public void store(String path, String fileName) throws IOException {
        this.path = path;

        ByteBuffer buffer;
        int size = FileMarker.length;   // File Format Marker

        size += Integer.SIZE / 8;   //Version

        size += Integer.SIZE / 8;   //id

        size += Long.SIZE / 8;  //time

        size += Float.SIZE / 8; //Unit-Milli-Voltage
        size += Float.SIZE / 8; //Samples-Per-Second

        size += ByteBufferUtils.getSize(subFolderName);

        size += ByteBufferUtils.getSize(eventDefineFileName);
        size += ByteBufferUtils.getSize(eventDataFileName);
        size += ByteBufferUtils.getSize(ecgSignalFileName);
        size += ByteBufferUtils.getSize(beatDetectFileName);


        buffer = ByteBuffer.allocate(size);
        buffer.put(FileMarker);
        buffer.putInt(Version);

        buffer.putInt(id);

        buffer.putLong(ecgStartTimeMS);

        buffer.putFloat(unitMilliVoltage);
        buffer.putFloat(ecgDeviceSamplesPerSecond);

        ByteBufferUtils.encode(subFolderName, buffer);

        ByteBufferUtils.encode(eventDefineFileName, buffer);
        ByteBufferUtils.encode(eventDataFileName, buffer);
        ByteBufferUtils.encode(ecgSignalFileName, buffer);
        ByteBufferUtils.encode(beatDetectFileName, buffer);

        hostOS.storeFile(path, fileName, buffer.array(), false);
    }

    public static HPatchData restore(HPatchHostOS hostOS, String path, String fileName) throws IOException {
        byte[] data = hostOS.restoreFile(path, fileName, 0, 0);
        if (data.length < 8) {
            throw new IOException("Invalid Data Length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] fileMarker = new byte[FileMarker.length];
        buffer.get(fileMarker);
        if (!Arrays.equals(fileMarker, FileMarker)) {
            throw new IOException("Invalid Format");
        }

        int version = buffer.getInt();
        if (version != Version) {
            throw new IOException("Invalid Format Version");
        }

        int id = buffer.getInt();

        long ecgStartTimeMS = buffer.getLong();

        float unitMilliVoltage = buffer.getFloat();
        float ecgDeviceSamplesPerSecond = buffer.getFloat();

        String subFolderName = ByteBufferUtils.decodeString(buffer);

        String eventDefineFileName = ByteBufferUtils.decodeString(buffer);
        String eventDataFileName = ByteBufferUtils.decodeString(buffer);
        String ecgSignalFileName = ByteBufferUtils.decodeString(buffer);
        String beatDetectFileName = ByteBufferUtils.decodeString(buffer);


        HPatchData hPatchData = new HPatchData(hostOS, null, id, unitMilliVoltage, ecgDeviceSamplesPerSecond, ecgDeviceSamplesPerSecond);

        hPatchData.ecgStartTimeMS = ecgStartTimeMS;

        hPatchData.fileName = fileName;
        hPatchData.subFolderName = subFolderName;

        hPatchData.eventDefineFileName = eventDefineFileName;
        hPatchData.eventDataFileName = eventDataFileName;
        hPatchData.ecgSignalFileName = ecgSignalFileName;
        hPatchData.beatDetectFileName = beatDetectFileName;

        //hPatchData.recreate(path);

        return hPatchData;
    }

    private String getDateText(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        return sdf.format(new Date(t));
    }

    public void setEcgTransferSamplesPerSecond(float ecgTransferSamplesPerSecond) {
        this.ecgTransferSamplesPerSecond = ecgTransferSamplesPerSecond;
    }
}
