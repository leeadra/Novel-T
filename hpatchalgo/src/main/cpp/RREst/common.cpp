#include "common.h"

void rangeFinder(float* data, int num_data, int currIdx, int onset, int offset, int* output, int init)
{
	int i;
	
	if (currIdx >= num_data) {
		printf("Invalid Data!!\n");
	}

	else {
		for (i = currIdx+1; i < num_data; i++) {
			
			if ((data[i - 1] <= (float)onset) & (data[i] >= (float)onset)) {
				output[0] = i;
			}
			else if (init == 1) {
				output[0] = 0;
			}

			if ((data[i - 1] <= (float)offset)&(data[i] >= (float)offset)) {
				output[1] = i - 1;
				break;
			}
		}
	}

	if ((output[0] == -1) | (output[1] == -1)) {
		printf("Invalid Data!!\n");
	}
}

void cropDatafloat(float * data, int sIdx, int eIdx, float * Cropped)
{
	int i;
	int num_data;

	num_data = eIdx - sIdx + 1;
	for (i = 0; i < num_data; i++) {
		Cropped[i] = data[sIdx + i];
	}
}

void cropDataint(int * data, int sTime, int Fs, int sIdx, int eIdx, int * Cropped)
{
	int i;
	int num_data;

	num_data = eIdx - sIdx + 1;

	for (i = 0; i < num_data; i++) {
		Cropped[i] = data[sIdx + i] - sTime*Fs;
	}
}

void normalize(float *data, int num_data, float *normalized) {
	int i;
	float sum_x, sum_xsq;
	float meanX, stdX;

	sum_x = 0;
	sum_xsq = 0;

	for (i = 0; i < num_data; i++) {
		sum_x = sum_x + data[i];
		sum_xsq = sum_xsq + data[i] * data[i];
	}

	meanX = sum_x / num_data;
	stdX = sum_xsq / num_data - meanX*meanX;
	stdX = sqrt(stdX);


	for (i = 0; i < num_data; i++) {
		normalized[i] = (data[i] - (meanX - stdX)) / (2 * stdX);
	}
}

void dbg_ExportFile(char *fname, float* data, int num_data) {
	FILE *fp;
	fp = fopen(fname, "wb");
	fwrite(data, sizeof(float), num_data, fp);
	fclose(fp);
}

void convFFT(float* sig1, float* sig2, int nn, float *output) {
	
	int i;

	for (i = 0; i < nn; i++) {
		output[2 * i + 1] = sig1[2 * i + 1] * sig2[2 * i + 1] - sig1[2 * i + 2] * sig2[2 * i + 2];
		output[2 * i + 2] = sig1[2 * i + 1] * sig2[2 * i + 2] + sig1[2 * i + 2] * sig2[2 * i + 1];
	}
}

int peak_detector(float* src_data, int data_size, int interval_size, float lowerFreq, float upperFreq, int Fs, int max_peak_num, float* peak_val, int* peak_index)
{
	int i;
	int peak_count = 0;
	int is_peak = 1;
	int index;
	int low_ind, up_ind;

	float resF;
	float sum;
	float sum_sq;
	float mean;
	float std;
	float min_height;

	resF = (float)Fs / data_size;

	low_ind = (int)(lowerFreq / resF + 0.5);
	up_ind = (int)(upperFreq / resF);
	
	sum = 0;
	sum_sq = 0;

	for (i = low_ind; i <= up_ind; i++) {
		sum = sum+src_data[i];
		sum_sq = sum_sq + src_data[i] * src_data[i];	
	}
	
	mean = sum / (up_ind - low_ind + 1);
	std = sum_sq / (up_ind - low_ind + 1) - mean*mean;
	std = sqrt(std);

	min_height = 1.5 * std;

	i = low_ind;

	while (i <= up_ind)
	{
		is_peak = 1;

		for (index = i - interval_size; index <= i + interval_size; index++)
		{
			if (index == i)
			{
				continue;
			}

			if ((src_data[index] > src_data[i]) || (src_data[i] < min_height))
			{
				is_peak = 0;
				break;
			}
		}

		if ((is_peak == 1) && (!(src_data[i] == 0)))
		{
			peak_val[peak_count] = src_data[i];
			peak_index[peak_count] = i;
			peak_count++;

			if (peak_count == max_peak_num)
			{
				break;
			}
		}

		i++;
	}

	if (peak_count != 0)
	{
		quick_sort(peak_val, peak_index, 0, peak_count - 1, 1);
	}

	return peak_count;
}

void quick_sort(float *data, int *loc, int low, int high, int sort_type)
{
	//sort_type=0: ascend, sort_type=1: descend
	int p = 0;
	if (low >= high) return;

	p = Partition(data, loc, low, high, sort_type);

	quick_sort(data, loc, low, p - 1, sort_type);
	quick_sort(data, loc, p + 1, high, sort_type);
}

void swap(float *data, int *loc, int i, int j)
{
	int tmp_loc;
	float tmp = data[i];
	data[i] = data[j];
	data[j] = tmp;
	if (loc != NULL)
	{
		tmp_loc = loc[i];
		loc[i] = loc[j];
		loc[j] = tmp_loc;
	}
}

int Partition(float *data, int *loc, int low, int high, int sort_type)
{
	int i;
	int p = low;
	for (i = p + 1; i <= high; ++i)
	{
		if (sort_type == 0)
		{
			if (data[i] < data[p])
			{
				swap(data, loc, i, p);
				if (i != p + 1)
				{
					swap(data, loc, i, p + 1);
				}
				p = p + 1;
			}
		}
		else
		{
			if (data[i] > data[p])
			{
				swap(data, loc, i, p);
				if (i != p + 1)
				{
					swap(data, loc, i, p + 1);
				}
				p = p + 1;
			}
		}
	}

	return p;
}

void normalize_freq(float *data, int nn, float scale, float *normalized) {
	int i;
	float RMS;
	float temp;

	RMS = 0;

	for (i = 0; i < nn; i++) {
		temp = data[2 * i + 1] * data[2 * i + 1] + data[2 * i + 2] * data[2 * i + 2];
		RMS = RMS + temp;
	}

	RMS = sqrt(RMS/nn);

	for (i = 0; i < nn; i++) {
		normalized[2 * i + 1] = scale * data[2 * i + 1] / RMS;
		normalized[2 * i + 2] = scale * data[2 * i + 2] / RMS;
	}
}
void windowing(float *data, int num_data, float* output) {
	
	int i;

	for (i = 0; i < num_data; i++) {
		output[i] = WIN[i] * data[i];
	}
}