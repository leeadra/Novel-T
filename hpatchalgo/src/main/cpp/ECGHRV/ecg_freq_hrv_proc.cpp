//#include <Windows.h>
#include <stdio.h>
#include <malloc.h>
#include <string.h>

#include "lib_statistics.h"
#include "lib_data.h"
#include "math.h"

#define PI       3.14159265358979323846

const int WELCH_WIN_SIZE = 128;
const int WELCH_WIN_STEP = 64;
const float HAMMING_WIN[WELCH_WIN_SIZE] = {	0.080000, 0.080563, 0.082250, 0.085057, 0.088978, 0.094002, 0.100118, 0.107311
											, 0.115562, 0.124852, 0.135157, 0.146454, 0.158713, 0.171906, 0.185999, 0.200959
											, 0.216749, 0.233329, 0.250660, 0.268699, 0.287402, 0.306723, 0.326615, 0.347029
											, 0.367915, 0.389223, 0.410899, 0.432892, 0.455146, 0.477608, 0.500223, 0.522935
											, 0.545689, 0.568429, 0.591100, 0.613645, 0.636010, 0.658141, 0.679982, 0.701480
											, 0.722584, 0.743240, 0.763399, 0.783012, 0.802030, 0.820406, 0.838097, 0.855058
											, 0.871247, 0.886627, 0.901158, 0.914805, 0.927535, 0.939317, 0.950121, 0.959922
											, 0.968695, 0.976419, 0.983075, 0.988647, 0.993121, 0.996486, 0.998734, 0.999859
											, 0.999859, 0.998734, 0.996486, 0.993121, 0.988647, 0.983075, 0.976419, 0.968695
											, 0.959922, 0.950121, 0.939317, 0.927535, 0.914805, 0.901158, 0.886627, 0.871247
											, 0.855058, 0.838097, 0.820406, 0.802030, 0.783012, 0.763399, 0.743240, 0.722584
											, 0.701480, 0.679982, 0.658141, 0.636010, 0.613645, 0.591100, 0.568429, 0.545689
											, 0.522935, 0.500223, 0.477608, 0.455146, 0.432892, 0.410899, 0.389223, 0.367915
											, 0.347029, 0.326615, 0.306723, 0.287402, 0.268699, 0.250660, 0.233329, 0.216749
											, 0.200959, 0.185999, 0.171906, 0.158713, 0.146454, 0.135157, 0.124852, 0.115562
											, 0.107311, 0.100118, 0.094002, 0.088978, 0.085057, 0.082250, 0.080563, 0.080000
											};

void spec_est(float* spec_data, float* src_data, int data_size, int fs, int nfft)
{
	int freq_index;
	int psd_num = (nfft + 1) / 2;
	//int n_max = nfft;
	float sum_re, sum_im;
	int data_index;
	float delta_t = 1 / (float)fs;
	float freq_res = (float)fs / (float)nfft;
	float freq = 0;
	float max_power = 0;
	int dominant_freq_index = 0;

	float temp;

	for (freq_index = 0; freq_index < psd_num; freq_index++)
	{
		sum_re = 0;
		sum_im = 0;

		for (data_index = 0; data_index < data_size; data_index++)
		{
			temp = 2 * PI * freq * (float)data_index * delta_t;

			sum_re += src_data[data_index] * cos(temp);
			sum_im -= src_data[data_index] * sin(temp);
		}
		spec_data[freq_index] = (sum_re * sum_re + sum_im * sum_im);

		freq += freq_res;

	}

}
void compute_periodogram(float* x, float* win, int win_size, int nfft, int fs, float* P)
{
	int index;
	int psd_num = (nfft + 1) / 2;
	float* xw = (float*)malloc(sizeof(float)*win_size);

	float U = 0;

	for (index = 0; index < win_size; index++)
	{
		xw[index] = x[index] * win[index];
		U += win[index] * win[index];
	}

	spec_est(P, xw, win_size, fs, nfft);

	for (index = 0; index < psd_num; index++)
	{
		P[index] = P[index] / U;
	}

	free(xw);

}
void p_welch(float* src_data, int data_size, int nfft, int fs, float* Pxx)
{
	float k_f;
	int k;
	int index, index_p;
	int start_index;
	int psd_num = (nfft + 1) / 2;
	float* Sxx = (float*)malloc(sizeof(float)*psd_num);
	float* Sxxk = (float*)malloc(sizeof(float)*psd_num);
	int size_minus_step = WELCH_WIN_SIZE - WELCH_WIN_STEP;

	memset(Sxx, 0, sizeof(float)*psd_num);
	memset(Sxxk, 0, sizeof(float)*psd_num);
	
	//Compute the number of segments
	k_f = (float)(data_size - WELCH_WIN_STEP) / (float)(WELCH_WIN_SIZE - WELCH_WIN_STEP);
	k = (int)k_f;


	for (index = 0; index < k; index++)
	{
		start_index = index*size_minus_step;
		compute_periodogram(src_data + start_index, (float*)HAMMING_WIN, WELCH_WIN_SIZE, nfft, fs, Sxxk);

		for (index_p = 0; index_p < psd_num; index_p++)
		{
			Sxx[index_p] += Sxxk[index_p];
		}
	}

	for (index_p = 0; index_p < psd_num; index_p++)
	{
		Sxx[index_p] = Sxx[index_p] / k;

		if (index_p == 0)
		{
			Pxx[index_p] = Sxx[index_p]/fs;
		}
		else
		{
			Pxx[index_p] = Sxx[index_p]*2 / fs;
		}
	}

	free(Sxx);
	free(Sxxk);
}


void calc_areas(float* psd, int n_psd, int fs, float* a_vlf, float* a_lf, float* a_hf)
{
	const float VLF_MIN = 0.00333;
	const float VLF_MAX = 0.04;
	const float LF_MIN = 0.04;
	const float LF_MAX = 0.15;
	const float HF_MIN = 0.15;
	const float HF_MAX = 0.4;

	int index; 
	float freq = 0;

	float delta_f = (float)fs / (float)(n_psd * 2 - 1);

	*a_vlf = 0;
	*a_lf = 0;
	*a_hf = 0;

	for (index = 0; index < n_psd; index++)
	{
		if (freq >= VLF_MIN && freq < VLF_MAX)
		{
			*a_vlf += delta_f * (psd[index] + psd[index + 1]) / 2;
		}
		else if (freq >= LF_MIN && freq < LF_MAX)
		{
			*a_lf += delta_f * (psd[index] + psd[index + 1]) / 2;
		}
		else if (freq >= HF_MIN && freq < HF_MAX)
		{
			*a_hf += delta_f * (psd[index] + psd[index + 1]) / 2;
		}

		freq += delta_f;
	}

}

void ecg_freq_hrv_proc(int reset, float* rri, int data_size, float* a_vlf, float* a_lf, float* a_hf)
{
	const float FS_INTP = 2;
	const int NPSD = 1024;
	int index;
	float rri_mean;
	int intp_size = 0;
	float* t = (float*)malloc(sizeof(float)*data_size);
	float* rri_norm = (float*)malloc(sizeof(float)*data_size);
	float* PSD = (float*)malloc(sizeof(float)*NPSD);
	

	t[0] = 0;
	for (index = 1; index < data_size; index++)
	{
		t[index] = (rri[index - 1] / 1000.0f) + t[index - 1];		
	}

	intp_size = (int)t[data_size - 1] * (int)FS_INTP + 1;

	float* t_intp = (float*)malloc(sizeof(float)*intp_size);
	float* rri_intp = (float*)malloc(sizeof(float)*intp_size);

	//interpolated time
	t_intp[0] = 0;
	for (index = 1; index < intp_size; index++)
	{
		t_intp[index] = t_intp[index - 1] + (1 / FS_INTP);
	}

	//Zero-mean RRI
	rri_mean = mean_f32(rri, data_size);
	for (index = 0; index < data_size; index++)
	{
		rri_norm[index] = rri[index] - rri_mean;
	}

	//linear_intp(t, rri_norm, data_size, rri_intp, (int)FS_INTP, intp_size);
	spline(t, rri_norm, data_size, t_intp, rri_intp, intp_size);

	//Zero-mean RRI
	rri_mean = mean_f32(rri_intp, intp_size);
	for (index = 0; index < intp_size; index++)
	{
		rri_intp[index] = rri_intp[index] - rri_mean;
	}
	/*
	//fprintf
	char fname1[] = "out1.txt";
	FILE *f1;
	f1 = fopen(fname1, "w");
	if (f1 == NULL)
	{
		return;
	}
	for (index = 0; index < intp_size; index++)
	{
		fprintf(f1, "%f\n", rri_intp[index]);
		
	}
	fclose(f1);

	char fname2[] = "out2.txt";
	FILE *f2;
	f2 = fopen(fname2, "w");
	if (f2  == NULL)
	{
		return;
	}
	for (index = 0; index < intp_size; index++)
	{
		fprintf(f2, "%f\n", rri_intp_s[index]);
	}
	fclose(f2);

	*/
	p_welch(rri_intp, intp_size, NPSD * 2 - 1, FS_INTP, PSD);
	calc_areas(PSD, NPSD, FS_INTP, a_vlf, a_lf, a_hf);

	free(t);
	free(rri_norm);
	free(t_intp);
	free(rri_intp);
	free(PSD);
}