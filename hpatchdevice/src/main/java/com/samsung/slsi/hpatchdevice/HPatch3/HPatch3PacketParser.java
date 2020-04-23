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
 * @file		HPatch3PacketParser.java
 * @brief		SPatch3 Packet Parser
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

package com.samsung.slsi.hpatchdevice.HPatch3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HPatch3PacketParser {
    private final static String TAG = HPatch3PacketParser.class.getSimpleName();

    private List<Byte> buffer = new LinkedList<>();
    private List<Byte> packetBuffer = new ArrayList<>();
    private final List<HPatch3Packet> packetList = new ArrayList<>();

    public void add(byte[] data) {
        for (byte aData : data) {
            buffer.add(aData);
        }
        parse();
    }

    private void parse() {
        while (buffer.size() >= 4) {
            if(isSyncStart(buffer)) {
                packetBuffer = new ArrayList<>();

                for (int i = 0; i < 4; i++) {
                    buffer.remove(0);
                }
            } else if(isSyncEnd(buffer)) {
                try {
                    packetList.add(new HPatch3Packet(packetBuffer));
                } catch (Exception e) {
                    //log(e.getLocalizedMessage());
                    e.printStackTrace();
                }

                for (int i = 0; i < 4; i++) {
                    buffer.remove(0);
                }
            } else {
                packetBuffer.add(buffer.remove(0));
            }
        }
    }

    public HPatch3Packet get() {
        if (packetList.size() == 0) {
            return null;
        } else {
            return packetList.remove(0);
        }
    }


    private boolean isSyncStart(List<Byte> buffer) {
        return buffer.get(0) == (byte)0x55 & buffer.get(1) == (byte)0xaa & buffer.get(2) == (byte)0xff & buffer.get(3) == (byte)0xff;
    }

    private boolean isSyncEnd(List<Byte> buffer) {
        return buffer.get(0) == (byte)0x44 & buffer.get(1) == (byte)0x99 & buffer.get(2) == (byte)0xee & buffer.get(3) == (byte)0xee;
    }
}
