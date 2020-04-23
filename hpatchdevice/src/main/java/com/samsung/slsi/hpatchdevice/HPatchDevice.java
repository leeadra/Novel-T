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
 * @file		HPatchDevice.java
 * @brief		HPatchDevice base class
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

package com.samsung.slsi.hpatchdevice;

import com.samsung.slsi.HPatch;
import com.samsung.slsi.HPatchBLECharacteristicObserver;
import com.samsung.slsi.HPatchBLEException;
import com.samsung.slsi.HPatchBeatDetectObserver;
import com.samsung.slsi.HPatchConnectionObserver;
import com.samsung.slsi.HPatchDeviceBLEInfo;
import com.samsung.slsi.HPatchError;
import com.samsung.slsi.HPatchHeartRateObserver;
import com.samsung.slsi.HPatchHostBLE;
import com.samsung.slsi.HPatchECGObserver;
import com.samsung.slsi.HPatchLeadDetect;
import com.samsung.slsi.HPatchStatusObserver;

import java.util.ArrayList;

public abstract class HPatchDevice
        implements HPatch, HPatchBLECharacteristicObserver {

    private float ecgUnitMilliVoltage;
    private float deviceECGSamplesPerSecond;
    private float transferECGSamplesPerSecond;
    private int transferECGSamplesPerPacket;

    private HPatchLeadDetect leadDetect = null;

    protected HPatchDeviceBLEInfo deviceBLEInfo;
    protected HPatchHostBLE hPatchHostBLE;

    private boolean isConnected = false;

    private ArrayList<HPatchConnectionObserver> hPatchConnectionObservers = new ArrayList<>();
    private ArrayList<HPatchECGObserver> hPatchECGObservers = new ArrayList<>();
    private ArrayList<HPatchHeartRateObserver> hPatchHeartRateObservers = new ArrayList<>();
    private ArrayList<HPatchStatusObserver> hPatchStatusObservers = new ArrayList<>();
    private ArrayList<HPatchBeatDetectObserver> hPatchBeatDetectObservers = new ArrayList<>();


    protected abstract void initialize() throws HPatchBLEException;
    protected abstract void clear();
    protected abstract void onReadBLE(byte[] packet);


    public HPatchDevice(HPatchDeviceBLEInfo deviceBLEInfo,
                        HPatchHostBLE hPatchHostBLE,
                        float ecgUnitMilliVoltage,
                        float deviceECGSamplesPerSecond,
                        float ecgTransferSamplesPerSecond,
                        int ecgTransferSamplesPerPacket,
                        HPatchLeadDetect leadDetect) {
        this.deviceBLEInfo = deviceBLEInfo;
        this.hPatchHostBLE = hPatchHostBLE;

        this.ecgUnitMilliVoltage = ecgUnitMilliVoltage;
        this.deviceECGSamplesPerSecond = deviceECGSamplesPerSecond;

        setECGTransferSamplingRate(ecgTransferSamplesPerSecond, ecgTransferSamplesPerPacket);

        this.leadDetect = leadDetect;
    }

    protected void setECGTransferSamplingRate(float samplesPerSecond,
                                              int samplesPerPacket) {
        this.transferECGSamplesPerSecond = samplesPerSecond;
        this.transferECGSamplesPerPacket = samplesPerPacket;
    }

    protected void write(byte[] packet) throws HPatchBLEException {
        hPatchHostBLE.write(deviceBLEInfo.address, packet);
    }

    protected void broadcastECGReceived(int sequence, int[] ecgSignal) {
        for (HPatchECGObserver observer : hPatchECGObservers) {
            observer.onUpdateECG(this, sequence, ecgSignal);
        }
    }

    protected void broadcastECGPacketLost(int lastSequence, int currentSequence) {
        for (HPatchECGObserver observer : hPatchECGObservers) {
            observer.onSPatchECGPacketLost(this, lastSequence, currentSequence);
        }
    }

    protected void broadcastHeartRateReceived(int heartRate) {
        for (HPatchHeartRateObserver observer : hPatchHeartRateObservers) {
            observer.onHeartRateReceived(this, heartRate);
        }
    }

    protected void broadcastBatteryRatioUpdated(int batteryRatio) {
        for (HPatchStatusObserver observer : hPatchStatusObservers) {
            observer.updateBatteryRatio(this, batteryRatio);
        }
    }

    protected void broadcastBLEConnectionUpdated(boolean isBLEConnected) {
        for (HPatchStatusObserver observer : hPatchStatusObservers) {
            observer.updateBLEConnectionStatus(this, isBLEConnected);
        }
    }

    protected void broadcastLeadStatusUpdated(int leadStatus) {
        for (HPatchStatusObserver observer : hPatchStatusObservers) {
            observer.updateLeadContactStatus(this, leadStatus);
        }
    }

    protected void broadcastError(HPatchError id, String message) {
        for (HPatchStatusObserver observer : hPatchStatusObservers) {
            observer.onError(this, id, message);
        }
    }

    protected void broadcastSPatchDeviceInformationUpdated(HPatch hPatch) {
        for (HPatchStatusObserver observer : hPatchStatusObservers) {
            observer.updateSPatchDeviceInformation(hPatch);
        }
    }

    protected void broadcastBeatDetectionUpdated(int[] index, float[] rri) {
        for (HPatchBeatDetectObserver observer : hPatchBeatDetectObservers) {
            observer.onUpdateBeatDetectData(index, rri);
        }
    }

    @Override
    public HPatchDeviceBLEInfo getBLEInfo() {
        return deviceBLEInfo;
    }

    @Override
    public int getId() {
        return HPatchUtil.getMinor(deviceBLEInfo.scanRecord);
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void connect() throws HPatchBLEException {
        hPatchHostBLE.connectBLE(deviceBLEInfo);
        hPatchHostBLE.addBLEReadObserver(deviceBLEInfo.address, this);

        initialize();
    }

    @Override
    public void disconnect() {
        clear();

        hPatchHostBLE.removeBLEReadObserver(deviceBLEInfo.address, this);
        hPatchHostBLE.disconnectBLE(deviceBLEInfo);

        isConnected = false;
    }

    public float getEcgUnitMilliVoltage() {
        return ecgUnitMilliVoltage;
    }

    public float getDeviceECGSamplesPerSecond() {
        return deviceECGSamplesPerSecond;
    }
    protected void setDeviceECGSamplesPerSecond(float samplesPerSecond) {
        this.deviceECGSamplesPerSecond = samplesPerSecond;
    }

    @Override
    public float getECGTransferSamplesPerSecond() {
        return transferECGSamplesPerSecond;
    }
    protected void setECGTransferSamplesPerSecond(float samplesPerSecond) {
        this.transferECGSamplesPerSecond = samplesPerSecond;
    }

    @Override
    public int getECGTransferSamplesPerPacket() {
        return transferECGSamplesPerPacket;
    }
    protected void setECGTransferSamplesPerPacket(int samplesPerPacket) {
        this.transferECGSamplesPerPacket = samplesPerPacket;
    }

    @Override
    public HPatchLeadDetect getLeadDetect() {
        return leadDetect;
    }

    @Override
    public void addConnectionObserver(HPatchConnectionObserver observer) {
        hPatchConnectionObservers.add(observer);
    }

    @Override
    public void removeConnectionObserver(HPatchConnectionObserver observer) {
        hPatchConnectionObservers.remove(observer);
    }

    @Override
    public void addECGObserver(HPatchECGObserver observer) {
        hPatchECGObservers.add(observer);
    }

    @Override
    public void removeECGObserver(HPatchECGObserver observer) {
        hPatchECGObservers.remove(observer);
    }

    @Override
    public void addHeartRateObserver(HPatchHeartRateObserver observer) {
        hPatchHeartRateObservers.add(observer);
    }

    @Override
    public void removeHeartRateObserver(HPatchHeartRateObserver observer) {
        hPatchHeartRateObservers.remove(observer);
    }

    @Override
    public void addStatusObserver(HPatchStatusObserver observer) {
        hPatchStatusObservers.add(observer);
    }

    @Override
    public void removeStatusObserver(HPatchStatusObserver observer) {
        hPatchStatusObservers.remove(observer);
    }

    @Override
    public void addBeatDetectObserver(HPatchBeatDetectObserver observer) {
        hPatchBeatDetectObservers.add(observer);
    }

    @Override
    public void removeBeatDetectObserver(HPatchBeatDetectObserver observer) {
        hPatchBeatDetectObservers.remove(observer);
    }

    @Override
    public void onCharacteristicRead(HPatchDeviceBLEInfo hPatchDeviceBLEInfo, byte[] packet) {
        if (isConnected()) {
            if (hPatchDeviceBLEInfo.address.equals(deviceBLEInfo.address)) {
                onReadBLE(packet);
            }
        }
    }

    void onConnected() {
        isConnected = true;

        for (HPatchConnectionObserver observer : hPatchConnectionObservers) {
            observer.onHPatchDeviceConnected(this);
        }
        broadcastBLEConnectionUpdated(true);
        broadcastSPatchDeviceInformationUpdated(this);
    }

    void onDisconnected() {
        isConnected = false;

        broadcastBLEConnectionUpdated(false);
        for (HPatchConnectionObserver observer : hPatchConnectionObservers) {
            observer.onHPatchDeviceDisconnected(this);
        }
    }

/*
    private FileLogUtil fileLogUtil = null;
    private String fileLogName;

    protected void log(String text) {
        if (fileLogName == null) {
            String timeText = FileLogUtil.getDateTextForFile(System.currentTimeMillis());
            fileLogName = "" + getId() + "_" + timeText + ".txt";
        }
        log(fileLogName, text);
    }

    protected void log(String fileName, String text) {
        if (fileLogUtil == null) {
            fileLogUtil = new FileLogUtil(Environment.getExternalStorageDirectory() + "/SPATCH_LOG/");
        }

        fileLogUtil.logging(fileName, text + "\n");
    }
*/
}
