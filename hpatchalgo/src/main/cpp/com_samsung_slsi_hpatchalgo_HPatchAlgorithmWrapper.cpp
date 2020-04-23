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
 * @file		com_samsung_slsi_spatchalgorithmbuild_SPatchAlgorithmWrapper.cpp
 * @brief		SPatch Algorithm Wrapper (JNI)
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2017/1/9
 *
 * <b>revision history :</b>
 * - 2017/1/9 First creation
 *******************************************************************************
 */

#include <stdint.h>
#include "com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper.h"
#include "spatchalgorithms.h"
#include "ECGFitness/ECGFitness_proc.h"

/*
 * Class:     com_samsung_slsi_spatchalgorithmbuild_SPatchAlgorithmWrapper
 * Method:    getRespirationRateEstimation
 * Signature: ([I[FII)F
 */
JNIEXPORT jfloat JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_getRespirationRateEstimation
        (JNIEnv *env, jclass, jintArray jRidx, jfloatArray jQRSminmax, jint jnum_data,
         jint jsecWin) {
    int *Ridx = new int[jnum_data];
    env->GetIntArrayRegion(jRidx, 0, jnum_data, Ridx);

    float *QRSminmax = new float[jnum_data];
    env->GetFloatArrayRegion(jQRSminmax, 0, jnum_data, QRSminmax);

    jfloat respirationRateEstimation = rrEst(Ridx, QRSminmax, jnum_data, jsecWin);

    delete[] Ridx;
    delete[] QRSminmax;

    return respirationRateEstimation;
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    getECGHeartRate
 * Signature: (I[III)I
 */
JNIEXPORT jint JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_getECGHeartRate
        (JNIEnv *env, jclass, jint reset, jintArray qrs_index, jint data_size, jint fs) {
    int *Ridx = new int[data_size];
    env->GetIntArrayRegion(qrs_index, 0, data_size, Ridx);

    jint heartRate = ecg_hr_proc(reset, Ridx, data_size, fs);

    delete[] Ridx;

    return heartRate;
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    getECGHeartRateVariability
 * Signature: (I[III)[F
 */
JNIEXPORT jfloatArray JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_getECGHeartRateVariability
        (JNIEnv *env, jclass, jint reset, jintArray qrs_index, jint data_size, jint fs, jint update_5_min) {
    int *Ridx = new int[data_size];
    env->GetIntArrayRegion(qrs_index, 0, data_size, Ridx);

    jfloatArray result = env->NewFloatArray(11);
    if (result == NULL) {
        return NULL;
    }

    float values[11];

    ecg_hrv_proc(reset, Ridx, data_size, fs, update_5_min,
            &values[0], &values[1], &values[2], &values[3], &values[4], &values[5],
            &values[6], &values[7],
            &values[8], &values[9], &values[10]);

    delete[] Ridx;

    env->SetFloatArrayRegion(result, 0, 11, values);
    return result;
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    getLeadOnOff
 * Signature: ([FIIF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_getLeadOnOff
        (JNIEnv *env, jclass, jfloatArray ecgSignal, jint num_data, jint fs, jfloat threshold) {
    float *signal = new float[num_data];
    env->GetFloatArrayRegion(ecgSignal, 0, num_data, signal);

//    jfloat nZCsPerSec = (jfloat)detectLeadOnOff(signal, num_data, fs, threshold);
    jboolean isLeadOn = (jboolean)detectLeadOnOff(signal, num_data, fs, threshold);

    delete[] signal;
    return isLeadOn;
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessCreateFitinfo
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessCreateFitinfo
        (JNIEnv *, jclass) {
    return (long)create_Fitinfo();
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessDestroyFitinfo
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessDestroyFitinfo
        (JNIEnv *, jclass, jlong handle) {
    destroy_Fitinfo((Fit_info*)(long)handle);
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessGetHRmax
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessGetHRmax
        (JNIEnv *, jclass, jlong handle, jint hrCurrent) {
    return getHRmax((Fit_info*)(long)handle, hrCurrent);
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessGetHRrest
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessGetHRrest
        (JNIEnv *, jclass, jlong handle, jint hrCurrent) {
    return getHRrest((Fit_info*)(long)handle, hrCurrent);
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessGetpctHR
 * Signature: (JII)F
 */
JNIEXPORT jfloat JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessGetpctHR
        (JNIEnv *, jclass, jlong handle, jint hrCurrent, jint age) {
    return getpctHR((Fit_info*)(long)handle, hrCurrent, age);
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessGetSportsZone
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessGetSportsZone
        (JNIEnv *, jclass, jlong handle) {
    return getSportsZone((Fit_info*)(long)handle);
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessGetMaxAvailableHR
 * Signature: (I)F
 */
JNIEXPORT jfloat JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessGetMaxAvailableHR
        (JNIEnv *, jclass, jint age) {
    return getMaxAvailableHR(age);
}

/*
 * Class:     com_samsung_slsi_spatchalgo_SPatchAlgorithmWrapper
 * Method:    ecgFitnessGetEnergyExpenditure
 * Signature: (JFIIIF)F
 */
JNIEXPORT jfloat JNICALL Java_com_samsung_slsi_hpatchalgo_HPatchAlgorithmWrapper_ecgFitnessGetEnergyExpenditure
        (JNIEnv *, jclass, jlong handle, jfloat avgHR, jint age, jint gender, jint w_kg, jfloat dur_min) {
    return getEnergyExpenditure((Fit_info*)(long)handle, avgHR, age, gender, w_kg, dur_min);
}
