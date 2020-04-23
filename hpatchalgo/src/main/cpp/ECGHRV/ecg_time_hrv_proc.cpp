#include <math.h>
#include <malloc.h>
#include <string.h>

#include "ecg_time_hrv_proc.h"
#include "lib_statistics.h"

//const int SIZE_24H = 12 * 24;
const int SIZE_24H = 12;

int g_counter_24h = 0;
float sdann_array[SIZE_24H] = { 0, };
float asdnn_array[SIZE_24H] = { 0, };

float rmssd_cal(float* rri, int data_size)
{
	int index;
	float* rri_diff_sq;
	float result = 0;
	float mean_val = 0;

	rri_diff_sq = (float*)malloc(sizeof(float)*(data_size - 1));
	
	for (index = 0; index < data_size-1; index++)
	{
		rri_diff_sq[index] = (rri[index + 1] - rri[index])*(rri[index + 1] - rri[index]);
	}

	mean_val = mean_f32(rri_diff_sq, data_size - 1);

	result = sqrt(mean_val);


	free(rri_diff_sq);

	return result;
}

void pnn50_calc(float* rri, int data_size, float* pnn50, float* nn50)
{
	int index;
	int nn_counter = 0;
	float rri_diff_abs = 0;


	for (index = 0; index < data_size - 1; index++)
	{
		rri_diff_abs = fabs(rri[index + 1] - rri[index]);

		if (rri_diff_abs > 50)
		{
			nn_counter++;
		}
	}
	
	*nn50 = nn_counter;
	*pnn50 = (float)nn_counter / ((float)data_size - 1.0f) * 100.0f;

}

float trapz(float* src_data, int data_size)
{
	int index;
	float z = 0;

	for (index = 0; index < data_size-1; index++)
	{
		z += (src_data[index] + src_data[index + 1]) / 2;
	}	

	return z;
}

float geo_measures(float*rri, int data_size)
{
	float dt = 0;
	float max_val, min_val;
	int max_index, min_index;
	int max_val_int32;
	int index;
	int peaki_counter = 0;
	int peaki_mean = 0;
	int buf_size;
	int m, n;
	float tinn;

	const float bin_width = 1.0f / 128.0f * 1000.0f;

	float* d;
	float* q;
	float* diff_sq;
	int* N;
	float* XOut;
	int* peaki;
	float delta;
	int bin_num;
	int i;
	float min_d = 0;
	int min_d_index;
	int n_min, m_min;

	max_val = max_f32(rri, data_size, &max_index);
	min_val = min_f32(rri, data_size, &min_index);

	dt = max_val - min_val;

	bin_num = (int)(dt / bin_width+0.5);

	if (bin_num <= 2)
	{
		return 0;
	}

	N = (int*)malloc(sizeof(int)*bin_num);
	q = (float*)malloc(sizeof(float)*bin_num);
	diff_sq = (float*)malloc(sizeof(float)*bin_num);
	XOut = (float*)malloc(sizeof(float)*bin_num);
	peaki = (int*)malloc(sizeof(int)*bin_num);

	hist(rri, data_size, bin_num, N, XOut);

	max_val_int32 = max_int32(N, bin_num, &max_index);

	for (index = 0; index < bin_num; index++)
	{
		if (N[index] == max_val_int32)
		{
			peaki[peaki_counter] = index;
			peaki_counter++;
		}
	}

	if (peaki_counter > 1)
	{
		peaki_mean = (int)(mean_int32(peaki, peaki_counter)+0.5);
	}
	else
	{
		peaki_mean = peaki[0];
	}

	if (peaki_mean < 1)
	{
		peaki_mean = 1;
	}
	else if (peaki_mean >= (bin_num - 1))
	{
		peaki_mean = bin_num - 2;
	}

	buf_size = peaki_mean * (bin_num - peaki_mean - 1)*3;
	d = (float*)malloc(sizeof(float)*buf_size);
	memset(d, 0, buf_size);
	i = 0;
	for (m = peaki_mean - 1; m >= 0; m--)
	{
		for (n = peaki_mean+1; n < bin_num; n++)
		{
			memset(q, 0, sizeof(float)*bin_num);

			delta = (float)N[peaki_mean] / ((float)peaki_mean - (float)m);
			for (index = 0; index < peaki_mean - m + 1; index++)
			{
				q[m + index] = (float)index*delta;
			}

			delta = (float)N[peaki_mean] / ((float)n - (float)peaki_mean);
			for (index = 0; index < n - peaki_mean + 1; index++)
			{
				q[peaki_mean + index] = N[peaki_mean] - (float)index*delta;
			}

			for (index = 0; index < bin_num; index++)
			{
				diff_sq[index] = ((float)N[index] - q[index])*((float)N[index] - q[index]);
			}

			d[i * 3 + 0] = trapz(diff_sq, bin_num);
			d[i * 3 + 1] = m;
			d[i * 3 + 2] = n;

			if (i == 0)
			{
				min_d = d[i * 3 + 0];
				min_d_index = i;
				m_min = d[i * 3 + 1];
				n_min = d[i * 3 + 2];
			}
			else if (min_d>d[i * 3 + 0])
			{
				min_d = d[i * 3 + 0];
				min_d_index = i;
				m_min = d[i * 3 + 1];
				n_min = d[i * 3 + 2];
			}

			i++;
		}
	}

	tinn = fabs(XOut[n_min] - XOut[m_min]);

	free(N);
	free(q);
	free(XOut);
	free(peaki);
	free(diff_sq);
	free(d);

	return tinn;
}

static float g_sdann_out = 0;
static float g_asdnn_out = 0;
void ecg_time_hrv_proc(int reset, int update_5_min, float* rri, int data_size, float* avnn, float* sdnn, float* sdann, float* asdnn, float* nn50, float* pnn50, float* rmssd, float* tinn)
{
	
	if (reset == 1)
	{
		g_counter_24h = 0;
		memset(sdann_array, 0, sizeof(float)*SIZE_24H);
		memset(asdnn_array, 0, sizeof(float)*SIZE_24H);
		g_sdann_out = 0;
		g_asdnn_out = 0;
	}

	if (update_5_min == 1)
	{
		//shift array
		memmove(sdann_array, sdann_array + 1, sizeof(float)*(SIZE_24H - 1));
		memmove(asdnn_array, asdnn_array + 1, sizeof(float)*(SIZE_24H - 1));
	}

	//AVNN
	*avnn = mean_f32(rri, data_size);

	//SDNN
	*sdnn = std_dev_f32(rri, data_size);

	//RMSSD
	*rmssd = rmssd_cal(rri, data_size);

	//pNN50
	pnn50_calc(rri, data_size, pnn50, nn50);

	
	//TINN
	*tinn = geo_measures(rri, data_size);

	if (update_5_min == 1)
	{
		//SDANN, ASDNN
		sdann_array[SIZE_24H - 1] = *avnn;
		asdnn_array[SIZE_24H - 1] = *sdnn;

		g_counter_24h++;

		if (g_counter_24h >= SIZE_24H)
		{
			g_sdann_out = std_dev_f32(sdann_array, SIZE_24H);
			g_asdnn_out = mean_f32(asdnn_array, SIZE_24H);
		}
		else
		{
			g_sdann_out = 0;
			g_asdnn_out = 0;
		}
	}
	*sdann = g_sdann_out;
	*asdnn = g_asdnn_out;
	return;
}