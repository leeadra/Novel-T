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
 * @file		HPatch3Packet.java
 * @brief		SPatch3 Packet Container
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HPatch3Packet {
    private final static String TAG = HPatch3Packet.class.getSimpleName();

    private HPatchPacketType type;
    private int length;

    private byte[] payload;

    private long timeMilli;

    private Map<HPatchPacketType, List<HPatch3Packet>> subPackets;

    private HPatch3Packet(List<Byte> data, long timeMilli) {
        this.timeMilli = timeMilli;
        parseData(data);
    }

    public HPatch3Packet(List<Byte> data) {
        this.timeMilli = System.currentTimeMillis();
        parseData(data);
    }

    private void parseData(List<Byte> data) {
        final int typeLengthSize = 1 + 2;

        if (data.size() < typeLengthSize) {
            throw new IllegalArgumentException("Too short packet");
        }

        type = HPatchPacketType.valueOf(data.get(0) & 0xFF);
        length = (data.get(1) & 0xFF) + (data.get(2) & 0xFF) * 0x100;

        if (length > data.size() - typeLengthSize) {
            throw new IllegalArgumentException("Invalid Packet Length"
                    + ": " + type + " Type"
                    + ", Expected: " + length
                    + ", Actual: " + (data.size() - typeLengthSize));
        } else {
            if (type == HPatchPacketType.Composite) {
                parsePackets(data);
            } else {
                payload = new byte[length];
                for (int i = 0; i < length && (i + typeLengthSize) < data.size(); i++) {
                    payload[i] = data.get(i + typeLengthSize);
                }
            }
        }
    }

    public HPatchPacketType getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPayloadAsInt8() {
        return (payload[0] & 0xFF);
    }

    public int getPayloadAsInt16() {
        return (payload[0] & 0xFF)
                | ((payload[1] & 0xFF) << 8);
    }

    public int[] getPayloadAsInt16Array() {
        int len = payload.length / 2;
        int[] data = new int[len];
        for (int i = 0; i < len; i++) {
            data[i] = (payload[i * 2] & 0x000000ff)
                    + ((payload[i * 2 + 1] << 8) & 0x0000ff00);
        }
        return data;
    }

    public int getPayloadAsInt32() {
        return (payload[0] & 0xFF)
                | ((payload[1] & 0xFF) << 8)
                | ((payload[2] & 0xFF) << 16)
                | ((payload[3] & 0xFF) << 24);
    }

    public float getPayloadAsFloat() {
        int n = (payload[0] & 0xFF)
                | ((payload[1] & 0xFF) << 8)
                | ((payload[2] & 0xFF) << 16)
                | ((payload[3] & 0xFF) << 24);
        return Float.intBitsToFloat(n);
    }

    public long getTimeMilli() {
        return timeMilli;
    }

    private void parsePackets(List<Byte> data) {
        subPackets = new HashMap<>();

        int i = 3;
        while (i < data.size()) {
            int next = i;
            HPatchPacketType t = HPatchPacketType.valueOf(data.get(i) & 0xFF);
            if (t == HPatchPacketType.Expansion) {
                //just one expansion is allowed
                next += 1 + 1 + 2 + (data.get(i + 2) & 0xFF) + (data.get(i + 3) & 0xFF) * 0x100;
            } else {
                next += 1 + 2 + (data.get(i + 1) & 0xFF) + (data.get(i + 2) & 0xFF) * 0x100;
            }

            if (next > data.size()) {
                throw new IllegalArgumentException("Invalid Packet Length: Actual: " + data.size() + ", Packet: " + next);
            }

            ArrayList<Byte> packetData = new ArrayList<>();
            for (; i < next; i++) {
                packetData.add(data.get(i));
            }

            List<HPatch3Packet> list = getSubPacket(t);
            if (list == null) {
                list = new ArrayList<>();
                subPackets.put(t, list);
            }
            list.add(new HPatch3Packet(packetData, getTimeMilli()));
        }
    }

    public Map<HPatchPacketType, List<HPatch3Packet>> getSubPackets() {
        return subPackets;
    }

    public List<HPatch3Packet> getSubPacket(HPatchPacketType type) {
        if (subPackets != null) {
            if (subPackets.containsKey(type)) {
                return subPackets.get(type);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public HPatch3Packet getFirstSubPacket(HPatchPacketType type) {
        if (subPackets != null) {
            if (subPackets.containsKey(type)) {
                return subPackets.get(type).get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public HPatch3Packet getSecondSubPacket(HPatchPacketType type) {
        if (subPackets != null) {
            if (subPackets.containsKey(type)) {
                return subPackets.get(type).get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
