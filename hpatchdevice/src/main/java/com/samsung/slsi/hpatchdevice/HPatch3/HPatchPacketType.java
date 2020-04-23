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
 * @file		HPatchPacketType.java
 * @brief		HPatch Packet Type Enumeration
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/30
 *
 * <b>revision history :</b>
 * - 2016/12/30 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchdevice.HPatch3;

import java.util.HashMap;
import java.util.Map;

public enum HPatchPacketType {

    ///////////////////////////////
    // Common Access Packet Types
    // 0x00 ~ 0x0F : 16

    KeepAlive(0x00),
    KeepAliveResponse(0x01),

    SWReset(0x02),
    ErrorNotification(0x03),

    PacketId(0x04),

    Composite(0x05),

    Read1ByteAddress(0x06),
    Read2ByteAddress(0x07),
    Read4ByteAddress(0x08),
    Read8ByteAddress(0x09),

    ReadResponse(0x0A),

    Write1ByteAddress(0x0B),
    Write2ByteAddress(0x0C),
    Write4ByteAddress(0x0D),
    Write8ByteAddress(0x0E),

    ///////////////////////////////
    // Streaming Packet Types
    // 0x10 ~ 0x1F : 16

    Stream8Bit(0x10),
    Stream16Bit(0x11),
    Stream24Bit(0x12),
    Stream32Bit(0x13),
    Stream64Bit(0x14),

    DataRequest(0x15),

    //////////////////////////////////
    // Data Description Packet Types
    // 0x20 ~ 0x3F : 32

    Source(0x20),
    Destination(0x21),
    Channel(0x22),
    SequenceNumber(0x23),
    ECGBeatDetect(0x24), // 0x24, Length(2byte), (uint32, float), ...
    IMURet(0x25),   // IMU_RET
    SkinTemperature(0x26),  //Payload: [temperature 4byte->float type]

    CheckSum(0x30),
    CRC(0x31),    //Cyclic Redundancy Check

    ////////////////////////////////////
    // Device Description Packet Types
    // 0x40 ~ 0x4F : 16

    Battery(0x40),

    RegisterHostKey(0x41),
    ResponseTransferKey(0x42),

    ////////////////////////////////////
    // User Define Packet Types
    // 0x50 ~ 0xFE

    BP_OTA_RSP(0x50),
    BP_OTA_Start(0x51),
    BP_OTA_Doing(0x52),
    BP_OTA_Stop(0x53),

    ///////////////////////////////
    // Expansion Packet Types
    Expansion(0xFF);


    ///////////////////////////////
    // Methods for value setting

    private final int value;
    HPatchPacketType(int value) {
        this.value = value;
    }
    public int getValue() { return value; }

    private static Map<Integer, HPatchPacketType> map = new HashMap<>();
    static {
        for (HPatchPacketType type : HPatchPacketType.values()) {
            map.put(type.value, type);
        }
    }
    public static HPatchPacketType valueOf(int type) {
        return map.get(type);
    }
}
