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
 * @file		HPatchEventDefine.java
 * @brief		HPatch Event Define Container - serialize
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HPatchEventDefine {

    private static final byte[] FileMarker = { 0x53, 0x50, 0x45, 0x44 };  // SPED : S-PATCH EVENT DEFINE
    private final HPatchHostOS hostOS;

    private String path;
    private String fileName;

    private final Map<Byte, String> idNameMap = new HashMap<>();
    private final Map<String, Byte> nameIdMap = new HashMap<>();

    public HPatchEventDefine(HPatchHostOS hostOS, String path, String fileName) {
        this.hostOS = hostOS;

        this.path = path;
        this.fileName = fileName;
    }

    public void add(byte id, String name) throws IOException {
        synchronized (idNameMap) {
            idNameMap.put(id, name);
            nameIdMap.put(name, id);
        }
        store(path, fileName);
    }

    public byte add(String name) throws IOException {
        byte id = 1;
        synchronized (idNameMap) {
            while (id != 0) {
                if (idNameMap.containsKey(id)) {
                    id++;
                } else {
                    break;
                }
            }
        }
        if (id != 0) {
            add(id, name);
        }
        return id;
    }

    public String get(byte id) {
        synchronized (idNameMap) {
            if (idNameMap.containsKey(id)) {
                return idNameMap.get(id);
            }
        }
        return "";
    }

    public byte get(String name) {
        synchronized (idNameMap) {
            if (nameIdMap.containsKey(name)) {
                return nameIdMap.get(name);
            }
        }
        return 0;
    }

    public void store(String path, String fileName) throws IOException {
        ByteBuffer buffer;
        int size = FileMarker.length + 4;   // File Format Marker + Total Count of Event-Defines
        synchronized (idNameMap) {
            for (byte id : idNameMap.keySet()) {
                size += 1;  //id
                size += ByteBufferUtils.getSize(idNameMap.get(id));    //Name Byte Length
            }

            buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(FileMarker);
            buffer.putInt(idNameMap.size());

            for (byte id : idNameMap.keySet()) {
                buffer.put(id);
                ByteBufferUtils.encode(idNameMap.get(id), buffer);
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

        synchronized (idNameMap) {
            idNameMap.clear();

            int count = buffer.getInt();
            for (int i = 0; i < count; i++) {
                byte id = buffer.get();
                String name = ByteBufferUtils.decodeString(buffer);

                idNameMap.put(id, name);
                nameIdMap.put(name, id);
            }
        }
    }
}
