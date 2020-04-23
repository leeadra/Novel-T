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
 * @file		HPatchEventsUnitTest.java
 * @brief		HPatchEvents UnitTest
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

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HPatchEventsUnitTest {

    private long time = 1;

    private byte id = 0x10;
    private float value = 12.34f;

    private byte[] encodedData = new byte[] {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
            0x01, 0x10, 0x41, 0x45, 0x70, (byte)0xA4
    };

    @Test
    public void setIdValueTest() {
        long time = System.currentTimeMillis();

        HPatchEvent event = new HPatchEvent(id, value);
        HPatchEvents events = new HPatchEvents(time, new HPatchEvent[] { event });

        assertEquals(time, events.getTime());
        assertEquals(1, events.getEvents().length);

        assertEquals(id, events.getEvents()[0].getId());
        assertEquals(value, events.getEvents()[0].getValue(), 0.0f);
    }

    @Test
    public void encodeTest() throws Exception {
        HPatchEvent event = new HPatchEvent(id, value);
        HPatchEvents events = new HPatchEvents(time, new HPatchEvent[] { event });

        ByteBuffer buffer = ByteBuffer.allocate(events.getByteSize());
        events.encode(buffer);

        byte[] actualData = buffer.array();

        assertArrayEquals(encodedData, actualData);
    }

    @Test
    public void getSizeTest() throws Exception {
        HPatchEvent event = new HPatchEvent(id, value);
        HPatchEvents events = new HPatchEvents(time, new HPatchEvent[] { event });

        assertEquals(encodedData.length, events.getByteSize());
    }

    @Test
    public void decodeStringTest() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(encodedData);
        HPatchEvents events = HPatchEvents.get(buffer);

        assertEquals(time, events.getTime());
        assertEquals(1, events.getEvents().length);

        assertEquals(id, events.getEvents()[0].getId());
        assertEquals(value, events.getEvents()[0].getValue(), 0.0f);
    }
}
