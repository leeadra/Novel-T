#include <malloc.h>

#include "ecg_hrv_pre_proc.h"
#include "ecg_hr_proc.h"
#include "lib_statistics.h"

int ecg_hr_calc(float* rri, int data_size)
{
	float rri_mean;
	int heart_rate; 

	rri_mean = mean_f32(rri, data_size);

	heart_rate = (int)(((1000.0f / rri_mean) * 60)+0.5);

	return heart_rate;
}

int ecg_hr_proc(int reset, int* qrs_index, int data_size, int fs)
{
	float* rri;
	int rri_size = data_size - 1;
	int heart_rate; 

	rri = (float*)malloc(sizeof(float)*rri_size);

	get_rri(qrs_index, fs, data_size, rri);
	heart_rate = ecg_hr_calc(rri, rri_size);

	free(rri);

	return heart_rate;
}