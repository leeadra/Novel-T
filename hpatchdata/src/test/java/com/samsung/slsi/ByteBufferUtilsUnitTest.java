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
 * @file		ByteBufferUtilsUnitTest.java
 * @brief		ByteBufferUtils UnitTest
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

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ByteBufferUtilsUnitTest {
    private String text = "Test String";
    private byte[] encodedData = new byte[] {
            0x00, 0x0B, 0x54, 0x65, 0x73, 0x74, 0x20, 0x53, 0x74, 0x72, 0x69, 0x6e, 0x67
    };

    @Test
    public void encodeTest() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(ByteBufferUtils.getSize(text));
        ByteBufferUtils.encode(text, buffer);

        byte[] actualData = buffer.array();

        assertArrayEquals(encodedData, actualData);
    }

    @Test
    public void getSizeTest() throws Exception {
        assertEquals(encodedData.length, ByteBufferUtils.getSize(text));
    }

    @Test
    public void decodeStringTest() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(encodedData);
        String actualText = ByteBufferUtils.decodeString(buffer);
        assertEquals(text, actualText);
        assertTrue(text.equals(actualText));
    }
}