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
 * @file		ByteBufferUtils.java
 * @brief		ByteBufferUtils to serialize string
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

public class ByteBufferUtils {
    private static final int textLengthFieldSize = 2;  //65535 / 2 limit for 2byte-encoding text

    public static void encode(String text, ByteBuffer buffer) {
        byte[] byteText = text.getBytes();
        buffer.putShort((short)(byteText.length & 0xFFFF));
        buffer.put(byteText);
    }

    public static int getSize(String text) {
        byte[] byteText = text.getBytes();
        return textLengthFieldSize + byteText.length;
    }

    public static String decodeString(ByteBuffer buffer) {
        int textLength = buffer.getShort() & 0xFFFF;
        byte[] byteText = new byte[textLength];
        buffer.get(byteText);
        return new String(byteText);
    }
}
