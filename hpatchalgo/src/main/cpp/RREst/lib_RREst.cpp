#include <math.h>
#include <stdlib.h>
#include "params.h"
#include "lib_matrix.h"
#include "common.h"
#include "lib_fft.h"
#include "InvMat.h"
#include "lib_RREst.h"
#include "invCoef.h"

void linearInt(float *data_t, float *data_v, int num_data, float *output, int Fs, int secWin)
{
	int i, k, ix, cnt;
	int nSize;
	float sum, mean;
	
	sum = 0;
	mean = 0;
	cnt = 0;

	float* t;

	t = (float*)malloc(sizeof(float)*(num_data+1));
	nSize = Fs * secWin;

	for (i = 0; i < num_data; i++) {
		t[i] = data_t[i] * Fs;
	}
	t[num_data] = 0;
	
	ix = 0;

	for (k = 0; k < nSize; k++) {

		if (ix < num_data) {
			if ((k < t[ix])&(ix == 0)) {
				output[k] = data_v[0];
			}
			else if ((k >= t[ix])&(k <= t[ix + 1])) {
				output[k] = (data_v[ix + 1] - data_v[ix])*(k - t[ix]) / (t[ix + 1] - t[ix]) + data_v[ix];
			}
			else {
				output[k] = -1;
			}

			if (((k + 1) > t[ix + 1])&(output[k]!= - 1)) {
				ix = ix + 1;
			}

		}

		else if (k > t[num_data - 1]) {
			output[k] = data_v[num_data-1];
		}
		else {
			output[k] = t[0];
		}
	}
	
	for (i = 0; i < nSize; i++) {
		if (output[i] > 0) {
			sum = sum + output[i];
			cnt++;
		}
	}

	mean = sum / cnt;

	for (i = 0; i < nSize; i++) {
		if (output[i] < 0) {
			output[i] = mean;
		}

	}
	free(t);
}

void SSA(float *data, int num_data, int maxGroupNum, float *output, int minTh) {

	int i, k, m;
	int indL, indK;
	int group_num;
	int Lp, Kp;
	int group_index, group_start_index;
	int index;

	float X[SIZE_K][SIZE_L] = { 0.0f, };
	float U[SIZE_L][SIZE_L] = { 0.0f, };
	float V[SIZE_K][SIZE_L] = { 0.0f, };
	float sigma[SIZE_L];
	float sigma_percent[SIZE_L];
	float dummy_array[SIZE_L];
	float sev;
	float sigma_percent_sum[MAX_GROUP_NUM];
	float sigma_percent_diff;
	float rc_mat[SIZE_L][SIZE_K];
	float sum;

	int group[SIZE_L];
	int group_cnt = 0;

	// Step 1 : L-trajectory matrix
	for (indL = 0; indL < SIZE_L; indL++)
	{
		for (indK = 0; indK < SIZE_K; indK++)
		{
			X[indK][indL] = data[indK + indL];
		}
	}

	// Step 2 : SVD (Singular Value Decomposition)
	Singular_Value_Decomposition((float*)X, SIZE_K, SIZE_L, (float*)V, sigma, (float*)U, dummy_array);


	// Step 3 : Grouping
	sev = 0;
	
	for (indL = 0; indL < SIZE_L; indL++)
	{
		sev += sigma[indL];
	}

	for (indL = 0; indL < SIZE_L; indL++)
	{
		sigma_percent[indL] = sigma[indL] / sev * 100;

		if (indL == 0)
		{
			group_num = 1;
			group[indL] = group_num;

			group_cnt = 1;
			sigma_percent_sum[group_num - 1] = sigma_percent[indL];

		}
		else
		{
			sigma_percent_diff = (float)fabs((double)sigma_percent[indL] - (double)sigma_percent[indL - 1]);
			if ((sigma_percent_diff <= minTh) && (group_cnt<2))
			{
				group[indL] = group_num;
				group_cnt++;
				sigma_percent_sum[group_num - 1] += sigma_percent[indL];
			}
			else
			{
				group_num++;
				group[indL] = group_num;
				group_cnt = 1;
				sigma_percent_sum[group_num - 1] = sigma_percent[indL];
			}

		}

		if (group_num == MAX_GROUP_NUM)
			break;
	}

	//Step 4 : Reconstruction Matrix
	if (SIZE_K > SIZE_L)
	{
		Lp = SIZE_L;
		Kp = SIZE_K;
	}
	else
	{
		Lp = SIZE_K;
		Kp = SIZE_L;
	}

	// Initialize output
	for (i = 0; i < num_data*maxGroupNum; i++) {
		output[i] = 0;
	}

	group_start_index = 0;
	for (group_index = 0; group_index < group_num; group_index++) {
		// Reconstruction Matrix
		for (indL = 0; indL < SIZE_L; indL++)
		{
			for (indK = 0; indK < SIZE_K; indK++)
			{
				sum = 0;
				for (index = group_start_index; group[index] == (group_index + 1); index++)
				{
					sum += U[indL][index] * V[indK][index] * sigma[index];

				}
				rc_mat[indL][indK] = sum;
			}
		}

		group_start_index = index;

		for (k = 0; k < Lp - 1; k++)
		{
			for (m = 0; m <= k; m++)
			{
				output[num_data*group_index + k] += (1 / ((float)k + 1)) * rc_mat[m][k - m];
			}
		}

		for (k = Lp - 1; k < Kp; k++)
		{
			for (m = 0; m < Lp; m++)
			{
				output[num_data*group_index + k] += (1 / (float)Lp) * rc_mat[m][k - m];
			}
		}

		for (k = Kp; k < num_data; k++)
		{
			for (m = k - Kp + 1; m < num_data - Kp + 1; m++)
			{
				output[num_data*group_index + k] += (1 / (float)(num_data - k)) * rc_mat[m][k - m];
			}
		}

		
	}
}

void linearIntRSA(int *data, int num_data, float *output, int Fs,  int REFS, int secWin) {
	int i;

	float *data_t;
	float *data_v;

	data_t = (float*)malloc(sizeof(float)*(num_data - 1));
	data_v = (float*)malloc(sizeof(float)*(num_data - 1));

	for (i = 0; i < num_data-1; i++) {
		data_v[i] = (float)(data[i + 1] - data[i]) / Fs;
		data_t[i] = (float)data[i]/Fs;
	}

	linearInt(data_t, data_v, num_data-1, output, RE_FS, secWin);
	
	free(data_t);
	free(data_v);
}

float rrEst(int *Ridx,  float *QRSminmax, int num_data, int secWin) {
	int i;
	int maxSpecIdx;

	float *dataTS1, *dataTS2;
	float *dataFr1, *dataFr2, *dataFr3;
	float *dataFrNorm1, *dataFrNorm2;

	float *Ridx_time;
	float *SSAdecomposed, *spectralSSA;
	float BrPM;

	Ridx_time = (float*)malloc(sizeof(float)*num_data);
	dataTS1 = (float*)malloc(sizeof(float)*RE_FS*secWin);
	dataTS2 = (float*)malloc(sizeof(float)*RE_FS*secWin);
	dataFr1 = (float*)malloc(sizeof(float) * (2 * NFFT + 1));
	dataFr2 = (float*)malloc(sizeof(float) * (2 * NFFT + 1));
	dataFr3 = (float*)malloc(sizeof(float) * (2 * NFFT + 1));
	dataFrNorm1 = (float*)malloc(sizeof(float) * (2 * NFFT + 1));
	dataFrNorm2 = (float*)malloc(sizeof(float) * (2 * NFFT + 1));
	SSAdecomposed = (float*)malloc(sizeof(float)*secWin*RE_FS*MAX_GROUP_NUM);
	spectralSSA = (float*)malloc(sizeof(float) * NFFT);

	for (i = 0; i < num_data; i++) {
		Ridx_time[i] = (float)Ridx[i] / FS;
	}

	// Linear Interpolation of RPA data
	linearInt(Ridx_time, QRSminmax, num_data, dataTS1, RE_FS, secWin);

	// Detrending the interpolated RPA data
	detrending(dataTS1, secWin*RE_FS, dataTS2);
	//	detrending_SPA(dataTS1, secWin*RE_FS, dataTS2);

	// Normalize the interpolated RPA data
	windowing(dataTS2, 256, dataTS1);

	// Spectral Analysis
	fft(dataTS1, secWin*RE_FS, dataFr1, NFFT, 1);
	
	// Normalize the transformed data
	normalize_freq(dataFr1, NFFT, 10, dataFrNorm1);

	// Linear Interpolation of RPA data
	linearIntRSA(Ridx, num_data, dataTS1, FS, RE_FS, secWin);

	// Detrending the interpolated data
	detrending(dataTS1, secWin*RE_FS, dataTS2);

	// Normalize the detrended data
	windowing(dataTS2, 256, dataTS1);

	// Spectral Analysis
	fft(dataTS1, secWin*RE_FS, dataFr2, NFFT, 1);

	normalize_freq(dataFr2, NFFT, 10, dataFrNorm2);

	// Convolution of RPA and RSA
	convFFT(dataFrNorm1, dataFrNorm2, NFFT, dataFr3);

	fft(dataFr3, secWin*RE_FS, dataTS1, NFFT, -1);

	// SSA
	SSA(dataTS1, secWin*RE_FS, MAX_GROUP_NUM, SSAdecomposed, 3);

	BrPM = calcBrPM(SSAdecomposed, secWin*RE_FS, NFFT, MAX_GROUP_NUM, 3, spectralSSA);

	free(dataTS1);
	free(dataTS2);
	free(dataFr1);
	free(dataFr2);
	free(dataFr3);
	free(dataFrNorm1);
	free(dataFrNorm2);
	free(Ridx_time);
	free(SSAdecomposed);
	free(spectralSSA);

	return BrPM;
}
void detrending(float *data, int num_data, float *detrended) {
	int row, col;
	int i;

	mat detrend;
	mat sig;
	mat detr_sig;

	InitMat(&detrend, SECWIN*RE_FS, SECWIN*RE_FS);
	InitMat(&sig, SECWIN*RE_FS, 1);
	InitMat(&detr_sig, SECWIN*RE_FS, 1);

	for (row = 0; row < detrend.rows; row++) {
		for (col = 0; col < detrend.cols; col++) {
			SetMat(&detrend, row, col, DetrendMat[col*SECWIN*RE_FS + row]);
		}
	}

	for (i = 0; i < SECWIN*RE_FS; i++) {
		SetMat(&sig, i, 0, (double)data[i]);
	}

	MulMat(&detrend, &sig, &detr_sig);

	for (i = 0; i < SECWIN*RE_FS; i++) {
		detrended[i] = (float)detr_sig.data[i][0];
	}

	FinalizeMat(&detrend);
	FinalizeMat(&sig);
	FinalizeMat(&detr_sig);
}

float calcBrPM(float *data, int num_data, int nfft, int tot_group, int target_num, float *output) {
	int i, j, k;
	int idx;
	float *transformed;
	float *estimate;
	float *cropped;
	float *absolute;
	float *peak_val;
	int *peak_idx;
	int peak_cnt;
	float BrPM;

	int sIdx, eIdx;
	int interval_size;
	float min_height;
	int max_peak_num;
	int maxSpecIdx;

	interval_size = 2;
	max_peak_num = 10;

	transformed = (float*)malloc(sizeof(float)*(2 * nfft + 1));
	estimate = (float*)malloc(sizeof(float)*num_data);
	cropped = (float*)malloc(sizeof(float)*num_data);
	absolute = (float*)malloc(sizeof(float)*nfft);
	peak_val = (float*)malloc(sizeof(float)*max_peak_num);
	peak_idx = (int*)malloc(sizeof(int)*max_peak_num);

	for (i = 0; i < num_data; i++) {
		estimate[i] = 0;
	}

	for (i = 0; i < target_num; i++) {

		sIdx = i*num_data;
		eIdx = i*num_data + num_data - 1;

		cropDatafloat(data, sIdx, eIdx, cropped);

		for (j = 0; j < num_data; j++) {
			estimate[j] = estimate[j] + cropped[j];
		}
	}

	fft(estimate, num_data, transformed, nfft, 1);

	for (i = 0; i < nfft; i++) {
		absolute[i] = sqrt((transformed[2 * i + 1] * transformed[2 * i + 1]) + (transformed[2 * i + 2] * transformed[2 * i + 2]));
	}

	peak_cnt = peak_detector(absolute, nfft, interval_size, (float)LOWER_FREQ, (float)UPPER_FREQ, (int)RE_FS, max_peak_num, peak_val, peak_idx);

	if (peak_cnt != 0) {
		maxSpecIdx = peak_idx[0];
		BrPM = (float)maxSpecIdx * ((float)RE_FS / NFFT) * 60;
	}

	else {
		maxSpecIdx = -1;
		BrPM = -1;
	}

	free(transformed);
	free(estimate);
	free(cropped);
	free(absolute);
	free(peak_val);
	free(peak_idx);

	return BrPM;
}