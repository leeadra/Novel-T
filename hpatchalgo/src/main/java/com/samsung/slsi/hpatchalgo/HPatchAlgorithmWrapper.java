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
 * @file		HPatchAlgorithmWrapper.java
 * @brief		SPatch Algorithm Wrapper
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/29
 *
 * <b>revision history :</b>
 * - 2016/12/24 First creation
 * - 2017/1/4   Add HR, HRV
 * - 2017/1/5   Add Lead On/Off
 * - 2017/1/6   Update Lead On/Off
 *******************************************************************************
 */

package com.samsung.slsi.hpatchalgo;

public class HPatchAlgorithmWrapper {

    /**
     * secWin : 32 sec. RRI values during 32 seconds for Respirate Calc.
     * Ridx : R peak index array in 32 sec.
     * QRSminmax : QRS Max - Min values in window. mapped with Ridx
     * num_data : peak count in window
     */
    public static native float getRespirationRateEstimation(int[] Ridx, float[] QRSminmax, int num_data, int secWin);

    //[HR]
    //reset: 초기에 한번만 reset
    //qrs_index: fw에서 받은 qrs_index 데이터 ( 4 sec )
    //data_size: qrs_index의 개수
    //fs: sample rate (ex. 256 samples/sec -> 256)
    public static native int getECGHeartRate(int reset, int[] qrs_index, int data_size, int fs);

    //[HRV]
    //reset: 초기에 한번만 reset
    //qrs_index: fw에서 받은 qrs_index 데이터 (5분)
    //data_size: qrs_index의 개수 (5분 데이터 개수)
    //fs: sample rate (ex. 256 samples/sec -> 256)
    //avnn ~ tinn: time domain HRV (각 값은 scalar인데 pointer로 넘겨주게 하였습니다.)
    //a_vlf ~ a_hf: frequency domain HRV (위와 동일, a_vlf: vlf power, a_lf: lf power, a_hf: hf power
    public static native float[] getECGHeartRateVariability(
            int reset,
            int[] qrs_index,
            int data_size,
            int fs,
            int update_5_min);


    // Lead On/Off
    // data: ECG Signal
    // num_data: count of ECG Signal (128 * 4)
    // Fs: sample rate
    // threshold: zero-crossing count threshold
    public static native boolean getLeadOnOff(float[] data, int num_data, int Fs, float threshold);


    public static native long ecgFitnessCreateFitinfo();
    public static native void ecgFitnessDestroyFitinfo(long p);

    public static native int ecgFitnessGetHRmax(long fitInfo, int HRcurrent);
    public static native int ecgFitnessGetHRrest(long fitInfo, int HRcurrent);
    public static native float ecgFitnessGetpctHR(long fitInfo, int HRcurrent, int age);
    public static native int ecgFitnessGetSportsZone(long fitInfo);
    public static native float ecgFitnessGetMaxAvailableHR(int age);
    public static native float ecgFitnessGetEnergyExpenditure(long fitInfo, float avgHR, int age, int gender, int w_kg, float dur_min);

    // Refer to below script to update .h
    //
    // cd /src/main/java
    // javah -o ../cpp/com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper.h com.samsung.slsi.spatchalgo.HPatchAlgorithmWrapper

    static {
        try {
            System.loadLibrary("hpatchalgo");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }
}
