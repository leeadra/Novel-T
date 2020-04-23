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
 * @file		HPatch3DataManager.java
 * @brief		SPatch3 DataManager
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/9
 *
 * <b>revision history :</b>
 * - 2016/11/9 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HPatch3DataManager
        implements
        HPatchECGObserver,
        HPatchBeatDetectObserver,
        HPatchHeartRateObserver,
        HPatchStatusObserver,

        HPatchAlgorithmResultObserver,

        HPatchECGDataManager,
        HPatchAlgorithmResultManager,

        HPatchDataManager
{
    private float unitMilliVoltage;

    private HPatch hPatch;
    private HPatchHostOS hostOS;
    private HostStorageUI hostStorageUI;

    private HPatchData hPatchData;

    private String path;

    private long firstTime;
    private long lastTime;
    private int sequenceIndex;

    private HPatchAlgorithmController algorithmController;

    private final ArrayList<HPatchHealthDataObserver> hPatchHealthDataObservers = new ArrayList<>();


    public HPatch3DataManager(HPatchHostOS hostOS, HPatch hPatch, String path) throws IOException {
        this.hostOS = hostOS;

        this.hPatch = hPatch;
        this.unitMilliVoltage = hPatch.getEcgUnitMilliVoltage();

        hPatchData = new HPatchData(hostOS, path,
                hPatch.getId(),
                hPatch.getEcgUnitMilliVoltage(),
                hPatch.getDeviceECGSamplesPerSecond(),
                hPatch.getECGTransferSamplesPerSecond());

        this.path = path;

        firstTime = 0;
    }

    public HPatch3DataManager(HPatchHostOS hostOS, HPatch hPatch, String path, HPatchData hPatchData) throws IOException {
        this.hostOS = hostOS;

        this.hPatch = hPatch;
        this.unitMilliVoltage = hPatch.getEcgUnitMilliVoltage();

        this.hPatchData = hPatchData;

        this.path = path;

        firstTime = hPatchData.getECGStartTime();
        updateLastTime(hPatch);
    }

    public void setHostStorageUI(HostStorageUI hostStorageUI) {
        this.hostStorageUI = hostStorageUI;
    }

    private final List<ECGSaveObserver> ecgSaveObservers = new ArrayList<>();
    public void addECGSaveObserver(ECGSaveObserver observer) {
        synchronized (ecgSaveObservers) {
            ecgSaveObservers.add(observer);
        }
    }

    public void removeECGSaveObserver(ECGSaveObserver observer) {
        synchronized (ecgSaveObservers) {
            ecgSaveObservers.remove(observer);
        }
    }

    public String getPath() {
        return path + "/" + hPatchData.getFileName();
    }

    public void setAlgorithmController(HPatchAlgorithmController algorithmController) {
        this.algorithmController = algorithmController;
    }

    private void broadcastECGUpdated(HPatch hPatch, int sequence, int[] ecgSignalData) {
        synchronized (hPatchHealthDataObservers) {
            for (HPatchHealthDataObserver observer : hPatchHealthDataObservers) {
                observer.onHPatchECGDataUpdated(this, hPatch, sequence, ecgSignalData);
            }
        }
    }

    private void broadcastAlgorithmUpdated(HPatchValueContainer result) {
        synchronized (hPatchHealthDataObservers) {
            for (HPatchHealthDataObserver observer : hPatchHealthDataObservers) {
                observer.onHPatchAlgorithmResultUpdated(this, result);
            }
        }
    }

    private void broadcastRRIUpdated(int[] rri) {
        synchronized (hPatchHealthDataObservers) {
            for (HPatchHealthDataObserver observer : hPatchHealthDataObservers) {
                observer.onHPatchRRIDataUpdated(rri);
            }
        }
    }

    @Override
    public void addAlgorithmResult(HPatchValueContainer results) {
        long timeMillisecond = System.currentTimeMillis();

        HPatchEventDefine eventDefine = hPatchData.getEventDefine();
        ArrayList<HPatchEvent> eventList = new ArrayList<>();

        for (int index = 0; index < results.getCount(); index++) {
            HPatchValue hPatchValue = results.getValue(index);
            String name = hPatchValue.getName();

            byte id = eventDefine.get(name);
            if (id == 0) {
                try {
                    id = eventDefine.add(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (id != 0) {
                try {
                    float value = hPatchValue.getValueAsFloat();
                    eventList.add(new HPatchEvent(id, value));
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }

        if (!eventList.isEmpty()) {
            HPatchEvent[] events = new HPatchEvent[eventList.size()];
            for (int i = 0; i < eventList.size(); i++) {
                events[i] = eventList.get(i);
            }

            try {
                if (hPatchData != null) {
                    HPatchEventData eventData = hPatchData.getEventData();
                    if (eventData != null) {
                        eventData.add(timeMillisecond, events);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public HPatchValueContainer getAlgorithmResult(long timeMillisecond) {
        HPatchSimpleValueContainer container = null;

        HPatchEventDefine eventDefine = hPatchData.getEventDefine();
        HPatchEventData eventData = hPatchData.getEventData();

        if (eventData != null) {
            int index = getNearEventIndex(eventData, timeMillisecond, 0, eventData.getTotalCount() - 1);
            if (index >= 0) {
                long searchRangeTime = 5 * 1000;   //ToDo: Adjust Search Range: now 5 sec
                long firstTime = timeMillisecond - searchRangeTime;
                int firstIndex = index;
                while (firstIndex > 0) {
                    if (eventData.get(firstIndex).getTime() < firstTime) {
                        break;
                    } else {
                        firstIndex--;
                    }
                }

                container = new HPatchSimpleValueContainer("" + timeMillisecond);
                for (int i = firstIndex; i <= index; i++) {
                    HPatchEvents events = eventData.get(i);
                    if (events != null) {
                        for (HPatchEvent e : events.getEvents()) {
                            String name = eventDefine.get(e.getId());
                            float value = e.getValue();

                            container.setValue(name).setValue(value);   //contains the last value
                        }
                    }
                }
            }
        }
        return container;
    }

    @Override
    public HPatchValueContainer[] getAlgorithmResult(long startTimeMillisecond, long endTimeMillisecond) {
        HPatchValueContainer[] values = null;

        HPatchEventDefine eventDefine = hPatchData.getEventDefine();
        HPatchEventData eventData = hPatchData.getEventData();

        if (eventData != null) {
            //System.out.println("Event: Search-End  : " + endTimeMillisecond + ", " + 0 + " ~ " + (eventData.getTotalCount() - 1));
            int endIndex = getNearEventIndex(eventData, endTimeMillisecond, 0, eventData.getTotalCount() - 1);

            //System.out.println("Event: Search-Start: " + startTimeMillisecond + ", " + 0 + " ~ " + endIndex);
            int startIndex = getNearEventIndex(eventData, startTimeMillisecond, 0, endIndex);

            //System.out.println("Event: " + startIndex + " ~ " + endIndex);
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (endIndex > eventData.getTotalCount()) {
                endIndex = eventData.getTotalCount();
            }
            if (endIndex >= 0 && eventData.getTotalCount() > 0) {
                int count = endIndex - startIndex + 1;
                values = new HPatchValueContainer[count];
                for (int i = 0; i < count; i++) {
                    HPatchEvents events = eventData.get(startIndex + i);

                    HPatchSimpleValueContainer container = new HPatchSimpleValueContainer("" + i);
                    container.setValue("TimeMS").setValue("" + events.getTime());
                    for (HPatchEvent e : events.getEvents()) {
                        String name = eventDefine.get(e.getId());
                        float value = e.getValue();

                        container.setValue(name).setValue(value);
                    }
                    values[i] = container;
                }
            }
        }
        return values;
    }

    private int getNearEventIndex(HPatchEventData eventData, long timeMillisecond, int left, int right) {
        if (left >= right) {
            return left;
        } else {
            long leftTime = eventData.get(left).getTime();
            long rightTime = eventData.get(right).getTime();
            int middle = (left + right) / 2;
            long middleTime = eventData.get(middle).getTime();
/*
            System.out.println("Event: T: " + TimeUtils.getDateText(timeMillisecond)
                    + ", L(" + left + "): " + TimeUtils.getDateText(leftTime)
                    + ", R(" + right + "): " + TimeUtils.getDateText(rightTime)
            );
*/
            if (timeMillisecond < leftTime) {
                return -1;
            } else if (timeMillisecond > rightTime) {
                return right;
            } else if (left == middle || middle == right) {
                return middle;
            } else if (leftTime <= timeMillisecond && timeMillisecond <= middleTime) {
                return getNearEventIndex(eventData, timeMillisecond, left, middle);
            } else {
                return getNearEventIndex(eventData, timeMillisecond, middle, right);
            }
        }
    }

    private HRCSVLogger hrCSVLogger;
    public static boolean isHRCSVLoggingEnabled;

    @Override
    public void onSPatchAlgorithmResultUpdated(HPatchValueContainer result) {
        if (result != null) {
            addAlgorithmResult(result);

            if (FileLogUtil.isInteralRelease && isHRCSVLoggingEnabled) { //Test for HR Logging
                HPatchValue heartRateValue = result.getValue("HeartRate");
                if (heartRateValue != null) {
                    try {
                        if (hrCSVLogger == null) {
                            /*hrCSVLogger = new HRCSVLogger(
                                    path + "HR/",
                                    "" + String.format(Locale.getDefault(), "%07d", hPatch.getId())
                                            + "_" + TimeUtils.getDateTextForFile(System.currentTimeMillis())
                                            + "_HR"
                                            + ".csv");*/
                        }
                        //hrCSVLogger.add(heartRateValue.getValueAsInteger());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            broadcastAlgorithmUpdated(result);
        }
    }

    @Override
    public long getFirstTimeMS() {
        return firstTime;
    }

    @Override
    public long getLastTimeMS() {
        return lastTime;
    }

    private int getEcgSignalIndex(long timeMilli) {
        long elapsedMilli = timeMilli - firstTime;
        return (int)(elapsedMilli * getSamplesPerSecond() / 1000);
    }

    @Override
    public int[] getECG(long firstTimeMS, long lastTimeMS) {
        int left = getEcgSignalIndex(firstTimeMS);
        int right = getEcgSignalIndex(lastTimeMS);

        return getSamples(left, right);
    }

    @Override
    public int getTotalSampleCount() {
        return hPatchData.getECGSignal().getTotalCount();
    }

    @Override
    public int[] getSamples(int firstIndex, int lastIndex) {
        int[] ecgSignal = null;

        int sampleCount = lastIndex - firstIndex;
        if (sampleCount > 0) {
            ecgSignal = new int[sampleCount];

            try {
                HPatchECGSignal ecg = hPatchData.getECGSignal();
                if (ecg != null) {
                    ecg.get(firstIndex, ecgSignal);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ecgSignal;
    }

    @Override
    public float getUnitMV() {
        return unitMilliVoltage;
    }

    @Override
    public void onUpdateECG(HPatch hPatch, int sequence, int[] ecgSignalData) {
        if (firstTime == 0) {
            firstTime = System.currentTimeMillis();
            sequenceIndex = 0;

            hPatchData.setECGStartTime(firstTime);
            hPatchData.setEcgTransferSamplesPerSecond(getSamplesPerSecond());
        }

        try {
            hPatchData.getECGSignal().add(ecgSignalData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sequenceIndex++;
        updateLastTime(hPatch);

        broadcastECGUpdated(hPatch, sequenceIndex, ecgSignalData);

        checkStorageState();
    }

    private void checkStorageState() {
        if (hostOS.getRemainedStorageMBSize() < 10) {
            hostStorageUI.onStorageAlmostFull(hostOS.getRemainedStorageMBSize());
        }
    }

    public void setFirstTime(long time) {
        firstTime = time;
        updateLastTime(hPatch);
    }

    private void updateLastTime(HPatch hPatch) {
        lastTime = firstTime + (long)((long) hPatchData.getECGSignal().getTotalCount() * 1000 / getSamplesPerSecond());
    }

    @Override
    public void onSPatchECGPacketLost(HPatch hPatch, int lastSequence, int currentSequence) {
//*
        int[] dummyEcgSignalData = new int[hPatch.getECGTransferSamplesPerPacket()];
        for (int sequence = lastSequence + 1; sequence < currentSequence; sequence++) {
            sequenceIndex++;
            updateLastTime(hPatch);

            try {
                hPatchData.getECGSignal().add(dummyEcgSignalData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            broadcastECGUpdated(hPatch, sequenceIndex, dummyEcgSignalData);
        }
//*/
    }

    //////////////////
    // Status Update

    @Override
    public void updateSPatchDeviceInformation(HPatch hPatch) {
    }

    private byte batteryRatioEventId;
    @Override
    public void updateBatteryRatio(HPatch hPatch, int batteryRatio) {
        if (batteryRatioEventId == 0) {
            HPatchEventDefine eventDefine = hPatchData.getEventDefine();
            try {
                batteryRatioEventId = eventDefine.add("Battery");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HPatchEvent[] events = new HPatchEvent[] {
                new HPatchEvent(batteryRatioEventId, batteryRatio)
        };

        try {
            hPatchData.getEventData().add(System.currentTimeMillis(), events);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte leadStatusEventId;
    @Override
    public void updateLeadContactStatus(HPatch sPatch, int leadStatus) {
        if (false) {
            boolean isOn = (leadStatus != 0);
            for (int i = 0; i < algorithmController.getAlgorithmCount(); i++) {
                algorithmController.getAlgorithm(i).enable(isOn);
            }
            if (!isOn) {
                algorithmController.updateAlgorithmResult(hPatchData.getBeatDetect().getBeatDetectLastTimeMS());
            }
        }

        if (leadStatusEventId == 0) {
            HPatchEventDefine eventDefine = hPatchData.getEventDefine();
            try {
                leadStatusEventId = eventDefine.add("Lead");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HPatchEvent[] events = new HPatchEvent[] {
                new HPatchEvent(leadStatusEventId, leadStatus)
        };

        try {
            hPatchData.getEventData().add(System.currentTimeMillis(), events);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateBLEConnectionStatus(HPatch hPatch, boolean isBLEConnected) {
        //Don't need to store Device BLE Connection Status
    }

    @Override
    public void onError(HPatch hPatch, HPatchError id, String message) {
    }

    private byte markEventId;
    long time;
    public long setMark() {
        if (markEventId == 0) {
            HPatchEventDefine eventDefine = hPatchData.getEventDefine();
            try {
                markEventId = eventDefine.add("Mark");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HPatchEvent[] events = new HPatchEvent[] {
                new HPatchEvent(markEventId, 1)
        };

        try {
            time = System.currentTimeMillis();
            hPatchData.getEventData().add(time, events);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return time;
    }

    private byte fallDownEventId;
    public void setFallDown() {
        if (fallDownEventId == 0) {
            HPatchEventDefine eventDefine = hPatchData.getEventDefine();
            try {
                fallDownEventId = eventDefine.add("FallDown");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HPatchEvent[] events = new HPatchEvent[] {
                new HPatchEvent(fallDownEventId, 1)
        };

        try {
            hPatchData.getEventData().add(System.currentTimeMillis(), events);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() throws InterruptedException {
        if (hPatchData != null) {
            hPatchData.clear();
        }
    }

    @Override
    public void onUpdateBeatDetectData(int[] timeIndex, float[] peakValue) {
        //System.out.println("BeatDetect: " + timeIndex.length + " samples are added. [0]: " + timeIndex[0]);
        if(timeIndex != null && peakValue != null) {
            try {
                hPatchData.getBeatDetect().add(timeIndex, peakValue);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (algorithmController != null) {
                algorithmController.updateAlgorithmResult(hPatchData.getBeatDetect().getBeatDetectLastTimeMS());
            }
            broadcastRRIUpdated(timeIndex);
        }
    }

    private int maxHR = Integer.MIN_VALUE;
    private int minHR = Integer.MAX_VALUE;

    @Override
    public void onHeartRateReceived(HPatch hPatch, int heartRate) {
        String name = "HR[" + hPatch.getId() + "]";
        HPatchValueContainer result = new HPatchSimpleValueContainer(name);

        result.setValue("HeartRate").setValue(heartRate);

        if (maxHR < heartRate) {
            maxHR = heartRate;
        }
        result.setValue("HeartRateMax").setValue(maxHR);

        if (minHR > heartRate) {
            minHR = heartRate;
        }
        result.setValue("HeartRateMin").setValue(minHR);

        //log(name + ": " + heartRate);
        if (algorithmController != null) {
            algorithmController.updateAlgorithmResult(result);
        }
    }

    @Override
    public String getRecordName() {
        return "" + hPatch.getId();
    }

    @Override
    public float getSamplesPerSecond() {
        return hPatch.getECGTransferSamplesPerSecond();
    }

    @Override
    public HPatchECGDataManager getECGDataManager() {
        return this;
    }

    @Override
    public HPatchBeatDetect getBeatDetect() {
        return hPatchData.getBeatDetect();
    }

    @Override
    public HPatchAlgorithmResultManager getAlgorithmResultManager() {
        return this;
    }

    @Override
    public void addObserver(HPatchHealthDataObserver observer) {
        synchronized (hPatchHealthDataObservers) {
            hPatchHealthDataObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(HPatchHealthDataObserver observer) {
        synchronized (hPatchHealthDataObservers) {
            hPatchHealthDataObservers.remove(observer);
        }
    }
}
