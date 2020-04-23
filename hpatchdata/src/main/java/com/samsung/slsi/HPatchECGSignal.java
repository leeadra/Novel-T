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
 * @file		HPatchECGSignal.java
 * @brief		HPatch ECG Signal Handler - cache
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/15
 *
 * <b>revision history :</b>
 * - 2016/12/15 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class HPatchECGSignal {

    private HPatchHostOS hostOS;

    private String path;
    private String fileName;
    private boolean isFirstData = true;

    private int limit;
    private int totalCount;

    private List<Integer> data = new LinkedList<>();
    private int dataIndex;

    private int[] cacheData;
    private int cacheIndex;

    HPatchECGSignal(HPatchHostOS hostOS, int limit) {
        this.hostOS = hostOS;

        if (limit <= 0) {
            limit = 1024;
        }
        this.limit = limit;
    }

    public void create(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;

        totalCount = 0;
    }

    public void restore(String path, String fileName) throws IOException {
        this.path = path;
        this.fileName = fileName;

        this.totalCount = hostOS.getFileSize(path, fileName) / 2;
    }

    public void add(int[] values) throws IOException {
        synchronized (this) {
            for (int v : values) {
                data.add(v);
            }
            totalCount += values.length;

            while (data.size() > limit) {
                data.remove(0);
                dataIndex++;
            }
        }
        store(values);
    }

    public int get(int index) throws IOException {
        synchronized (this) {
            if (index >= 0 && index < totalCount) {
                if (index >= dataIndex && (index - dataIndex) < data.size()) {
                    return data.get(index - dataIndex);
                } else {
                    //restoreCache(index);
                    return cacheData[index - cacheIndex];
                }
            } else {
                return 0;
            }
        }
    }

    public void get(int index, int[] ecgSignal) throws IOException {
        synchronized (this) {
            int i = 0;
            int count = ecgSignal.length;
            while (i < count) {
                int v = 0;
                if (index >= 0 && index < totalCount) {
                    if (index >= dataIndex && (index - dataIndex) < data.size()) {
                        for (int j = index - dataIndex; j < data.size() && i < count; j++) {
                            v = data.get(index - dataIndex);
                            ecgSignal[i] = v;

                            i++;
                            index++;
                        }
                    } else {
                        //restoreCache(index);
                        v = cacheData[index - cacheIndex];
                        ecgSignal[i] = v;

                        i++;
                        index++;
                    }
                } else {
                    ecgSignal[i] = v;

                    i++;
                    index++;
                }
            }
        }
    }

    private boolean isLive;
    private final Queue<int[]> storeQueue = new LinkedList<>();
    private Runnable storeRunnable = new Runnable() {
        @Override
        public void run() {
            int[] values = null;
            isLive = true;
            while (isLive) {
                synchronized (storeQueue) {
                    try {
                        storeQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!storeQueue.isEmpty()) {
                        values = storeQueue.remove();
                    }
                }

                if (values != null) {
                    byte[] buffer = new byte[values.length * 2];

                    int i = 0;
                    for (int value : values) {
                        //Little Endian
                        buffer[i] = (byte)((value) & 0x00ff);
                        buffer[i + 1] = (byte)((value >> 8) & 0x00ff);

                        i += 2;
                    }

                    boolean isAppend = !isFirstData;
                    /*try {
                        hostOS.storeFile(path, fileName, buffer, isAppend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/

                    if (isFirstData)
                        isFirstData = false;
                }
            }
        }
    };
    private Thread storeThread;

    private void store(int[] values) {
        if (storeThread == null) {
            storeThread = new Thread(storeRunnable);
            storeThread.start();
        }

        synchronized (storeQueue) {
            storeQueue.add(values);
            storeQueue.notifyAll();
        }
    }

    void clear() throws InterruptedException {
        if (isLive) {
            isLive = false;
        }
        if (storeThread != null) {
            if (storeThread.isAlive()) {
                synchronized (storeQueue) {
                    storeQueue.notifyAll();
                }
                storeThread.join();
            }
        }
    }

    private void restoreCache(int index) throws IOException {
        if (index < cacheIndex || cacheIndex + limit <= index
                || cacheData == null) {
            int readCount = limit;
            if (this.data.size() > 0 && dataIndex < index + limit) {
                readCount = dataIndex - index;
            }
            System.out.println("Cache-Fault: Index:" + index + ", Read: " + readCount);

            byte[] buffer = hostOS.restoreFile(path, fileName, index * 2, readCount * 2);
            cacheData = new int[limit];

            int i = 0;
            for (int j = 0; j < readCount && j < buffer.length / 2; j++) {
                //Little Endian
                cacheData[j] = (buffer[i] & 0xFF) + ((buffer[i + 1] << 8) & 0xFF00);
                i += 2;
            }
            if (readCount != limit) {
                i = 0;
                for (int j = readCount; j < limit && i < this.data.size(); j++) {
                    cacheData[j] = this.data.get(i);
                    i++;
                }
            }
            cacheIndex = index;
            System.out.println("Cache: " + index + " ~ " + (index + limit));
        }
    }

    int getTotalCount() {
        return totalCount;
    }
}
