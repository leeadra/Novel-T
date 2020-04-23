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
 * @file		LeadOnOff.java
 * @brief		Lead On/Off Algorithm Wrapper
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2017/1/4
 *
 * <b>revision history :</b>
 * - 2017/1/4 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchalgorithm;

import com.samsung.slsi.HPatch;
import com.samsung.slsi.HPatchECGObserver;
import com.samsung.slsi.HPatchLeadDetect;
import com.samsung.slsi.hpatchalgo.HPatchAlgorithmWrapper;

import java.util.LinkedList;
import java.util.List;

public class LeadOnOff
        implements
        HPatchLeadDetect,
        HPatchECGObserver {

    private static final int TimeRange = 5; // sec

    private final List<Integer> signals = new LinkedList<>();
    private Boolean isResultLeadOn = null;
    private boolean isLeadOn;
    private float threshold = 30;

    private final static int leadOnCountLimit = 5;
    private final static int leadOffCountLimit = 5;
    private int leadCount;

    @Override
    public Boolean isLeadOn() {
        return isResultLeadOn;
    }

    @Override
    public float getThreshold() {
        return threshold;
    }

    @Override
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    @Override
    public void onUpdateECG(HPatch hPatch, int sequence, int[] ecgSignalData) {
        float samplesPerSecond = hPatch.getECGTransferSamplesPerSecond();
        float[] ecgSignal;

        synchronized (signals) {
            for (int data : ecgSignalData) {
                signals.add(data);
            }

            int sampleLimit = (int)(samplesPerSecond * TimeRange);
            while (signals.size() > sampleLimit) {
                signals.remove(0);
            }

            ecgSignal = new float[signals.size()];
            for (int i = 0; i < signals.size(); i++) {
                ecgSignal[i] = signals.get(i);
            }
        }

        if (ecgSignal.length > 0) {
            boolean localLeadOn = HPatchAlgorithmWrapper.getLeadOnOff(ecgSignal, ecgSignal.length, (int) samplesPerSecond, threshold);

            if (isLeadOn) {
                if (localLeadOn) {
                    leadCount = 0;
                } else {
                    leadCount++;
                    if (leadCount > leadOffCountLimit) {
                        isLeadOn = false;
                        isResultLeadOn = false;
                        leadCount = 0;
                    }
                }
            } else {
                if (localLeadOn) {
                    leadCount++;
                    if (leadCount > leadOnCountLimit) {
                        isLeadOn = true;
                        isResultLeadOn = true;
                        leadCount = 0;
                    }
                } else {
                    leadCount = 0;
                }
            }
        }
    }

    @Override
    public void onSPatchECGPacketLost(HPatch hPatch, int lastSequence, int currentSequence) {
        synchronized (signals) {
            signals.clear();
        }
        isLeadOn = false;
    }
}
