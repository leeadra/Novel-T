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
 * @file		HPatchEvent.java
 * @brief		HPatch Event Data Handler
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

public class HPatchEvent {
    private byte id;
    private float value;

    public HPatchEvent(byte id, float value) {
        this.id = id;
        this.value = value;
    }

    public byte getId() {
        return id;
    }

    public float getValue() {
        return value;
    }

    public int getByteSize() {
        return (Byte.SIZE + Float.SIZE) / 8;
    }

    public void encode(ByteBuffer buffer) {
        buffer.put(id);
        buffer.putFloat(value);
    }

    public static HPatchEvent get(ByteBuffer buffer) {
        return new HPatchEvent(buffer.get(), buffer.getFloat());
    }
}