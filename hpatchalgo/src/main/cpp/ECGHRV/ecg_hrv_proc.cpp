//#include <Windows.h>
#include <malloc.h>


#include "ecg_hrv_pre_proc.h"
#include "ecg_time_hrv_proc.h"
#include "ecg_freq_hrv_proc.h"
#include "ecg_hrv_proc.h"

void ecg_hrv_proc(int reset, int* qrs_index, int data_size, int fs, int update_5_min,
	float* avnn, float* sdnn, float* sdann, float* asdnn, float* nn50, float* pnn50, float* rmssd, float* tinn, 
	float* a_vlf, float* a_lf, float* a_hf)
{
	float* rri;
	float* rri_e;
	int rri_size = data_size - 1;

	rri = (float*)malloc(sizeof(float)*rri_size);
	rri_e = (float*)malloc(sizeof(float)*rri_size);

	get_rri(qrs_index, fs, data_size, rri);
	ectopic_beat_removal(rri, rri_size, rri_e);
/*
	int index;
	char fname1[] = "rri.txt";
	FILE *f1;
	f1 = fopen(fname1, "w");
	if (f1 == NULL)
	{
		return;
	}
	for (index = 0; index < rri_size; index++)
	{
		fprintf(f1, "%f\n", rri[index]);

	}
	fclose(f1);
*/

	ecg_time_hrv_proc(reset, update_5_min, rri_e, rri_size, avnn, sdnn, sdann, asdnn, nn50, pnn50, rmssd, tinn);
	ecg_freq_hrv_proc(reset, rri_e, rri_size, a_vlf, a_lf, a_hf);
	

	free(rri);
	free(rri_e);
}