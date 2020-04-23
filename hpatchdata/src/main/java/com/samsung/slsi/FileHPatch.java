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
 * @file		FileHPatch.java
 * @brief		FileHPatch for loading from file
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

public class FileHPatch implements HPatch {

    private int id;

    private float unitMilliVoltage;
    private float samplesPerSecond;

    public FileHPatch(int id, float unitMilliVoltage, float samplesPerSecond) {
        this.id = id;

        this.unitMilliVoltage = unitMilliVoltage;
        this.samplesPerSecond = samplesPerSecond;
    }

    @Override
    public HPatchDeviceBLEInfo getBLEInfo() {
        return null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connect() throws HPatchBLEException {

    }

    @Override
    public void disconnect() {

    }

    public float getEcgUnitMilliVoltage() {
        return unitMilliVoltage;
    }

    public float getDeviceECGSamplesPerSecond() {
        return samplesPerSecond;
    }

    @Override
    public float getECGTransferSamplesPerSecond() {
        return samplesPerSecond;
    }

    @Override
    public int getECGTransferSamplesPerPacket() {
        return 0;
    }

    @Override
    public HPatchLeadDetect getLeadDetect() {
        return null;
    }

    @Override
    public void addConnectionObserver(HPatchConnectionObserver observer) {

    }

    @Override
    public void removeConnectionObserver(HPatchConnectionObserver observer) {

    }

    @Override
    public void addECGObserver(HPatchECGObserver observer) {

    }

    @Override
    public void removeECGObserver(HPatchECGObserver observer) {

    }

    @Override
    public void addHeartRateObserver(HPatchHeartRateObserver observer) {

    }

    @Override
    public void removeHeartRateObserver(HPatchHeartRateObserver observer) {

    }

    @Override
    public void addStatusObserver(HPatchStatusObserver observer) {

    }

    @Override
    public void removeStatusObserver(HPatchStatusObserver observer) {

    }

    @Override
    public void addBeatDetectObserver(HPatchBeatDetectObserver observer) {

    }

    @Override
    public void removeBeatDetectObserver(HPatchBeatDetectObserver observer) {

    }
}
