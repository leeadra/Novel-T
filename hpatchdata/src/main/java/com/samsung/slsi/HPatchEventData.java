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
 * @file		HPatchEventData.java
 * @brief		HPatch Event Data Container - serialize
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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class HPatchEventData {

    private static final byte[] FileMarker = { 0x53, 0x50, 0x45, 0x56 };  // SPEV : S-PATCH EVENT VALUE
    private final HPatchHostOS hostOS;

    private String path;
    private String fileName;

    private final ArrayList<HPatchEvents> totalEvents = new ArrayList<>();

    public HPatchEventData(HPatchHostOS hostOS, String path, String fileName) {
        this.hostOS = hostOS;

        this.path = path;
        this.fileName = fileName;
    }

    public void add(HPatchEvents events) throws IOException {
        synchronized (totalEvents) {
            totalEvents.add(events);
        }
        append(path, fileName, events);
    }

    private boolean isFirst = true;
    private void append(String path, String fileName, HPatchEvents events) throws IOException {
        if (isFirst) {
            isFirst = false;
            //store(path, fileName);
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(events.getByteSize()).order(ByteOrder.LITTLE_ENDIAN);
            events.encode(buffer);

            //hostOS.storeFile(path, fileName, buffer.array(), true);
        }
    }

    public void add(long time, HPatchEvent[] events) throws IOException {
        HPatchEvents hPatchEvents = new HPatchEvents(time, events);
        synchronized (totalEvents) {
            totalEvents.add(hPatchEvents);
        }
        append(path, fileName, hPatchEvents);
    }

    public HPatchEvents get(int index) {
        synchronized (totalEvents) {
            if (index >= 0 && index < totalEvents.size()) {
                return totalEvents.get(index);
            }
        }
        return null;
    }

    /**
     * Find events most near by time
     * time     specific time millisecond
     */
    public HPatchEvents get(long time) {
        synchronized (totalEvents) {
            int index = getIndex(time, 0, totalEvents.size() - 1);
            if (index < 0) {
                return null;
            } else {
                return totalEvents.get(index);
            }
        }
    }

    /**
     * Find specific id event before time
     */
    public HPatchEvent get(long time, byte id) {
        synchronized (totalEvents) {
            int index = getIndex(time, 0, totalEvents.size() - 1);
            while (index >= 0) {
                HPatchEvents events = totalEvents.get(index);
                for (HPatchEvent event : events.getEvents()) {
                    if (id == event.getId()) {
                        return event;
                    }
                }
            }
            return null;
        }
    }

    private int getIndex(long time, int left, int right) {
        if (left >= right) {
            return left;
        } else {
            int middle = (left + right) / 2;
            if (left == middle || middle == right) {
                return middle;
            } else {
                long leftTime = totalEvents.get(left).getTime();
                long rightTime = totalEvents.get(right).getTime();

                long middleTime = totalEvents.get(middle).getTime();

                if (time < leftTime || rightTime < time) {
                    return -1;
                } else if (leftTime <= time && time <= middleTime) {
                    return getIndex(time, left, middle);
                } else {
                    return getIndex(time, middle, right);
                }
            }
        }
    }

    public void store(String path, String fileName) throws IOException {
        ByteBuffer buffer;
        int size = FileMarker.length;   // File Format Marker

        synchronized (totalEvents) {
            for (HPatchEvents events : totalEvents) {
                size += events.getByteSize();
            }

            buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(FileMarker);

            for (HPatchEvents events : totalEvents) {
                events.encode(buffer);
            }
        }

        //hostOS.storeFile(path, fileName, buffer.array(), false);
    }

    public void restore() throws IOException {
        byte[] data = hostOS.restoreFile(path, fileName, 0, 0);
        if (data.length < 8) {
            throw new IOException("Invalid File Length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        byte[] fileMarker = new byte[FileMarker.length];
        buffer.get(fileMarker);
        if (!Arrays.equals(fileMarker, FileMarker)) {
            throw new IOException("Invalid File Format");
        }

        synchronized (totalEvents) {
            totalEvents.clear();

            while (buffer.hasRemaining()) {
                HPatchEvents events = HPatchEvents.get(buffer);
                totalEvents.add(events);
            }
        }
    }

    public int getTotalCount() {
        return totalEvents.size();
    }
}
