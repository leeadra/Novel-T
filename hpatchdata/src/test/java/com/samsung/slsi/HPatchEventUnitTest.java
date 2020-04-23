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
 * @file		HPatchEventUnitTest.java
 * @brief		HPatchEvent UnitTest
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class HPatchEventUnitTest {
    private byte id = 0x10;
    private float value = 12.34f;

    private byte[] encodedData = new byte[] {
            0x10, 0x41, 0x45, 0x70, (byte)0xA4
    };

    @Test
    public void setIdValueTest() {
        HPatchEvent event = new HPatchEvent(id, value);

        assertEquals(id, event.getId());
        assertEquals(value, event.getValue(), 0.0f);
    }

    @Test
    public void encodeTest() throws Exception {
        HPatchEvent event = new HPatchEvent(id, value);

        ByteBuffer buffer = ByteBuffer.allocate(event.getByteSize());
        event.encode(buffer);

        byte[] actualData = buffer.array();

        assertArrayEquals(encodedData, actualData);
    }

    @Test
    public void getSizeTest() throws Exception {
        byte id = 0x10;
        float value = 12.34f;
        HPatchEvent event = new HPatchEvent(id, value);

        assertEquals(encodedData.length, event.getByteSize());
    }

    @Test
    public void decodeStringTest() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(encodedData);
        HPatchEvent event = HPatchEvent.get(buffer);

        assertEquals(id, event.getId());
        assertEquals(value, event.getValue(), 0.0f);
    }
}