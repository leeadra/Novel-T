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
 * @file		com_samsung_slsi_SPatchAlgorithmWrapper.cpp
 * @brief		SPatch Algorithm Wrapper
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

#ifndef __SPATCH_ALGORITHMS_H__
#define __SPATCH_ALGORITHMS_H__

/**
 * secWin : 32 sec. RRI values during 32 seconds for Respirate Calc.
 * Ridx : R peak index array in 32 sec.
 * QRSminmax : QRS min-max values in window. mapped with Ridx
 * num_data : peak count in window
 */
extern float rrEst(int *Ridx, float *QRSminmax, int num_data, int secWin);

//[HR]
//reset: 초기에 한번만 reset
//qrs_index: fw에서 받은 qrs_index 데이터 ( 4 sec )
//data_size: qrs_index의 개수
//fs: sample rate (ex. 256 samples/sec -> 256)
extern int ecg_hr_proc(int reset, int *qrs_index, int data_size, int fs);

//[HRV]
//reset: 초기에 한번만 reset
//qrs_index: fw에서 받은 qrs_index 데이터 (5분)
//data_size: qrs_index의 개수 (5분 데이터 개수)
//fs: sample rate (ex. 256 samples/sec -> 256)
//avnn ~ tinn: time domain HRV (각 값은 scalar인데 pointer로 넘겨주게 하였습니다.)
//a_vlf ~ a_hf: frequency domain HRV (위와 동일, a_vlf: vlf power, a_lf: lf power, a_hf: hf power
extern void ecg_hrv_proc(int reset, int *qrs_index, int data_size, int fs, int update_5_min,
                         float *avnn, float *sdnn, float *sdann,
                         float *asdnn, float *nn50, float *pnn50,
                         float *rmssd, float *tinn,
                         float *a_vlf, float *a_lf, float *a_hf);


extern bool detectLeadOnOff(float *data, int num_data, int Fs, float threshold);

#endif