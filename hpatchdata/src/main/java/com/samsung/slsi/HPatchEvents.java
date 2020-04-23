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
 * @file		HPatchEvents.java
 * @brief		HPatch Events Container - serialize
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

import java.nio.ByteBuffer;

public class HPatchEvents {

    private long time;
    private HPatchEvent[] events;

    public HPatchEvents(long time, HPatchEvent[] events) {
        this.time = time;
        this.events = events;
    }

    public long getTime() {
        return time;
    }

    public HPatchEvent[] getEvents() {
        return events;
    }

    public int getByteSize() {
        int size = 0;

        size += Long.SIZE / 8;  //time
        size += 1;  // count of event
        for (HPatchEvent e : events) {
            size += e.getByteSize();
        }

        return size;
    }

    public void encode(ByteBuffer buffer) {
        buffer.putLong(time);
        buffer.put((byte)events.length);
        for (HPatchEvent e : events) {
            e.encode(buffer);
        }
    }

    public static HPatchEvents get(ByteBuffer buffer) {
        long time = buffer.getLong();
        int count = buffer.get() & 0xFF;
        HPatchEvent[] events = new HPatchEvent[count];
        for (int i = 0; i < count; i++) {
            events[i] = HPatchEvent.get(buffer);
        }
        return new HPatchEvents(time, events);
    }
}
