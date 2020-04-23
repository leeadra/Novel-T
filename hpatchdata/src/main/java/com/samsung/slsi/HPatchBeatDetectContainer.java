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
 * @file		HPatchBeatDetectContainer.java
 * @brief		Beat Detection Data Container
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

package com.samsung.slsi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

class HPatchBeatDetectContainer implements HPatchBeatDetect {
    private static int UnitSize = 4 * 2;    // Index:Int, Peak:float

    private HPatchHostOS hostOS;
    private float samplesPerSecond;

    private String path;
    private String fileName;
    private boolean isFirstData = true;

    private int limit;
    private int totalSize;

    private ArrayList<HPatchBeatDetectData> data = new ArrayList<>();
    private int dataIndex;

    private HPatchBeatDetectData[] cacheData;
    private int cacheIndex;

    private long firstTimeMS;
    private long lastTimeMS;

    private int firstTimeIndex;
    private int lastTimeIndex;


    HPatchBeatDetectContainer(HPatchHostOS hostOS, float samplesPerSecond, int limit) {
        this.hostOS = hostOS;
        this.samplesPerSecond = samplesPerSecond;

        if (limit <= 0) {
            limit = 1024;
        }
        this.limit = limit;
    }

    public void create(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;

        totalSize = 0;
    }


    public void add(int[] time, float[] peakValue) throws IOException {
        if (time.length > 0 && time.length == peakValue.length) {
            if (data.size() == 0) {
                firstTimeIndex = time[0];
                firstTimeMS = System.currentTimeMillis();
            }

            HPatchBeatDetectData[] containers = new HPatchBeatDetectDataContainer[time.length];
            for (int i = 0; i < time.length; i++) {
                containers[i] = new HPatchBeatDetectDataContainer(time[i] - firstTimeIndex, peakValue[i]);
            }

            synchronized (this) {
                int lastIndex = containers[containers.length - 1].getTimeIndex();
                lastTimeIndex = lastIndex;
                lastTimeMS = firstTimeMS + (long)(lastIndex * 1000 / samplesPerSecond);

                Collections.addAll(data, containers);
                totalSize += containers.length;

                while (data.size() > limit) {
                    data.remove(0);
                    dataIndex++;
                }
            }
            store(containers);
        }
    }

    public HPatchBeatDetectData get(int index) throws IOException {
        synchronized (this) {
            if (index >= 0 && index < totalSize) {
                if (index >= dataIndex && (index - dataIndex) < data.size()) {
                    return data.get(index - dataIndex);
                } else {
                    restoreCache(index);
                    return cacheData[index - cacheIndex];
                }
            } else {
                return null;
            }
        }
    }

    private boolean isLive;
    private final Queue<HPatchBeatDetectData[]> storeQueue = new LinkedList<>();
    private Runnable storeRunnable = new Runnable() {
        @Override
        public void run() {
            HPatchBeatDetectData[] values = null;
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
                    ByteBuffer byteBuffer = ByteBuffer.allocate(UnitSize * values.length).order(ByteOrder.LITTLE_ENDIAN);
                    for (HPatchBeatDetectData value : values) {
                        byteBuffer.putInt(value.getTimeIndex());
                        byteBuffer.putFloat(value.getPeakValue());
                    }

                    boolean isAppend = !isFirstData;
                    /*try {
                        hostOS.storeFile(path, fileName, byteBuffer.array(), isAppend);
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

    private void store(HPatchBeatDetectData[] containers) {
        if (storeThread == null) {
            storeThread = new Thread(storeRunnable);
            storeThread.start();
        }

        synchronized (storeQueue) {
            storeQueue.add(containers);
            storeQueue.notifyAll();
        }
    }

    void clear() throws InterruptedException {
        if (storeThread != null) {
            if (isLive || storeThread.isAlive()) {
                isLive = false;
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
            if (dataIndex < index + limit) {
                readCount = dataIndex - index;
            }
            //System.out.println("BeatDetectContainer: Cache-Fault: Index:" + index + ", Read: " + readCount);

            byte[] data = hostOS.restoreFile(path, fileName, index * UnitSize, readCount * UnitSize);
            cacheData = new HPatchBeatDetectData[limit];

            if(data != null) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

                for (int j = 0; j < readCount; j++) {
                    int time = byteBuffer.getInt();
                    float peak = byteBuffer.getFloat();

                    HPatchBeatDetectData value = new HPatchBeatDetectDataContainer(time, peak);

                    cacheData[j] = value;
                }
                if (readCount != limit) {
                    int i = 0;
                    for (int j = readCount; j < limit; j++) {
                        cacheData[j] = this.data.get(i);
                        i++;
                    }
                }
            }
            cacheIndex = index;
            //System.out.println("BeatDetectContainer: Cache: " + index + " ~ " + (index + limit));
        }
    }

    @Override
    public long getBeatDetectFirstTimeMS() {
        return firstTimeMS;
    }

    @Override
    public long getBeatDetectLastTimeMS() {
        return lastTimeMS;
    }

    @Override
    public HPatchBeatDetectData[] getBeatDetect(long firstTimeMS, long lastTimeMS) {
        HPatchBeatDetectData[] resultData = null;

        synchronized (this) {
            int firstTimeIndex = (int) ((firstTimeMS - this.firstTimeMS) * samplesPerSecond / 1000);
            int lastTimeIndex = (int) ((lastTimeMS - this.firstTimeMS) * samplesPerSecond / 1000);

            int firstIndex;
            try {
                firstIndex = getTargetIndex(firstTimeIndex);
            } catch (IOException e) {
                e.printStackTrace();
                firstIndex = -1;
            }
            if (firstIndex < 0) {
                firstIndex = 0;
            }

            int lastIndex;
            try {
                lastIndex = getTargetIndex(lastTimeIndex);
            } catch (IOException e) {
                e.printStackTrace();
                lastIndex = -1;
            }
            if (lastIndex < 0) {
                lastIndex = totalSize;
            }

            int count = lastIndex - firstIndex;
            if (count > 0) {
                int index;
                if (totalSize >= count) {
                    index = totalSize - count;
                } else {
                    index = 0;
                    count = totalSize;
                }
                //System.out.println("BeatDetectContainer: " + firstIndex + " ~ " + lastIndex + ", " + count);

                resultData = new HPatchBeatDetectData[count];
                for (int i = 0; i < count; i++) {
                    HPatchBeatDetectData d = null;
                    try {
                        d = get(index + i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    resultData[i] = d;
                }
            }
        }
        return resultData;
    }

    private int getTargetIndex(int targetTimeIndex) throws IOException {
        int index = getNearIndex(targetTimeIndex, dataIndex, totalSize - 1);
        if (index < 0) {
            index = getNearIndex(targetTimeIndex, cacheIndex, cacheIndex + limit - 1);
        }
        if (index < 0) {
            index = getNearIndex(targetTimeIndex, 0, totalSize - 1);
        }
        return index;
    }

    private int getNearIndex(int targetTimeIndex, int firstTargetIndex, int lastTargetIndex) throws IOException {
        if (firstTargetIndex >= lastTargetIndex) {
            return firstTargetIndex;
        } else {
            int middleTargetIndex = (firstTargetIndex + lastTargetIndex) / 2;

            int firstTimeIndex;
            HPatchBeatDetectData first = get(firstTargetIndex);
            if (first != null) {
                firstTimeIndex = first.getTimeIndex();
            } else {
                firstTimeIndex = -1;
            }

            int middleTimeIndex;
            HPatchBeatDetectData middle = get(middleTargetIndex);
            if (middle != null) {
                middleTimeIndex = middle.getTimeIndex();
            } else {
                middleTimeIndex = -1;
            }

            int lastTimeIndex;
            HPatchBeatDetectData last = get(lastTargetIndex);
            if (last != null) {
                lastTimeIndex = last.getTimeIndex();
            } else {
                lastTimeIndex = -1;
            }
/*
            System.out.println("Target: " + targetTimeIndex
                    + ", FTI: " + firstTargetIndex + "(" + firstTimeIndex + ")"
                    + ", LTI: " + lastTargetIndex + "(" + lastTimeIndex + ")"
                    + ", MTI: " + middleTargetIndex + "(" + middleTimeIndex + ")"
            );
*/
            if (firstTimeIndex <= targetTimeIndex && targetTimeIndex <= middleTimeIndex) {
                return getNearIndex(targetTimeIndex, firstTargetIndex, middleTargetIndex);
            } else if (middleTimeIndex <= targetTimeIndex && targetTimeIndex <= lastTimeIndex) {
                if (firstTargetIndex == middleTargetIndex) {
                    return middleTargetIndex;
                } else {
                    return getNearIndex(targetTimeIndex, middleTargetIndex, lastTargetIndex);
                }
            } else {
                return -1;
            }
        }
    }

    public void setSamplesPerSecond(float samplesPerSecond) {
        this.samplesPerSecond = samplesPerSecond;
    }
}
