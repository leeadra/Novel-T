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
 * @file		HPatchAlgorithmManager.java
 * @brief		HPatch Algorithm Manager
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

package com.samsung.slsi.hpatchalgorithm;

import com.samsung.slsi.HPatch;
import com.samsung.slsi.HPatchAlgorithm;
import com.samsung.slsi.HPatchAlgorithmController;
import com.samsung.slsi.HPatchAlgorithmResultObserver;
import com.samsung.slsi.HPatchBeatDetect;
import com.samsung.slsi.HPatchSimpleValueContainer;
import com.samsung.slsi.HPatchValueContainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;


public class HPatchAlgorithmManager
        implements HPatchAlgorithmController {

    private HPatchBeatDetect beatDetect;

    private ArrayList<HPatchAlgorithm> algorithms = new ArrayList<>();

    private ArrayList<HPatchAlgorithmResultObserver> observers = new ArrayList<>();

    private final Queue<HPatchValueContainer> targetResults = new LinkedList<>();

    private boolean isDirty;
    private boolean isLive;
    private final Runnable updateAlgorithmResultRunnable = new Runnable() {
        @Override
        public void run() {
            isLive = true;
            while (isLive)
            {
                synchronized (updateAlgorithmResultRunnable) {
                    try {
                        updateAlgorithmResultRunnable.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (isDirty && beatDetect != null)
                {
                    isDirty = false;

                    if (!targetResults.isEmpty()) {
                        HPatchValueContainer result = targetResults.remove();
                        long targetTime = beatDetect.getBeatDetectLastTimeMS();

                        for (HPatchAlgorithm algorithm : algorithms) {
                            algorithm.updateAlgorithmResult(beatDetect, targetTime, result);
                        }

                        broadcastResultUpdated(result);
                    }
                }
            }
        }
    };
    private Thread updateAlgorithmResultThread;


    private String getDateText(long t) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(t));
    }

    public HPatchAlgorithmManager(HPatch hPatch, HPatchBeatDetect beatDetect) {
        this.beatDetect = beatDetect;
//*
        algorithms.add(new HeartRateEstimation(hPatch.getDeviceECGSamplesPerSecond()));
        algorithms.add(new HeartRateVariability(hPatch.getDeviceECGSamplesPerSecond()));
        algorithms.add(new RespiratoryRateEstimation());
//*/
    }

    private void start() {
        updateAlgorithmResultThread = new Thread(updateAlgorithmResultRunnable);
        updateAlgorithmResultThread.start();
    }

    @Override
    public void updateAlgorithmResult(long targetTime) {
        updateAlgorithmResult(new HPatchSimpleValueContainer(String.valueOf(targetTime)));
    }

    @Override
    public void updateAlgorithmResult(HPatchValueContainer result) {
        isDirty = true;

        if (updateAlgorithmResultThread == null) {
            start();
        }

        synchronized (updateAlgorithmResultRunnable) {
            targetResults.add(result);
            updateAlgorithmResultRunnable.notifyAll();
        }
    }

    private void broadcastResultUpdated(HPatchValueContainer result) {
        for (HPatchAlgorithmResultObserver observer : observers) {
            observer.onSPatchAlgorithmResultUpdated(result);
        }
    }

    @Override
    public int getAlgorithmCount() {
        return algorithms.size();
    }

    @Override
    public HPatchAlgorithm getAlgorithm(int index) {
        if (index >= 0 && index < algorithms.size()) {
            return algorithms.get(index);
        } else {
            return null;
        }
    }

    @Override
    public HPatchAlgorithm getAlgorithm(String name) {
        for (HPatchAlgorithm algorithm : algorithms) {
            if (algorithm.getName().equals(name)) {
                return algorithm;
            }
        }
        return null;
    }

    @Override
    public void addObserver(HPatchAlgorithmResultObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(HPatchAlgorithmResultObserver observer) {
        observers.remove(observer);
    }

    public void clear() throws InterruptedException {
        if (updateAlgorithmResultThread != null) {
            isLive = false;
            updateAlgorithmResultThread.join();
        }
    }
}
