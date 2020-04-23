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
 * @file		HPatch.java
 * @brief		HPatch Device Interface
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

public interface HPatch {
    HPatchDeviceBLEInfo getBLEInfo();
    int getId();

    boolean isConnected();

    void connect() throws HPatchBLEException;
    void disconnect();

    float getEcgUnitMilliVoltage();
    float getDeviceECGSamplesPerSecond();
    float getECGTransferSamplesPerSecond();
    int getECGTransferSamplesPerPacket();

    HPatchLeadDetect getLeadDetect();

    void addConnectionObserver(HPatchConnectionObserver observer);
    void removeConnectionObserver(HPatchConnectionObserver observer);

    void addECGObserver(HPatchECGObserver observer);
    void removeECGObserver(HPatchECGObserver observer);

    void addHeartRateObserver(HPatchHeartRateObserver observer);
    void removeHeartRateObserver(HPatchHeartRateObserver observer);

    void addStatusObserver(HPatchStatusObserver observer);
    void removeStatusObserver(HPatchStatusObserver observer);

    void addBeatDetectObserver(HPatchBeatDetectObserver observer);
    void removeBeatDetectObserver(HPatchBeatDetectObserver observer);
}
