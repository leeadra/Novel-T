#include "LeadOnOffDetect.h"
#include <stdlib.h>
#include <math.h>

#define TEMP_SIZE 50
#define OFFSET 31
#define BLKSIZE 50
#define NUM_PKS 3
#define THRESHOLD 0.7

int cntZeroCrossing(float *data, int num_data) {
	int i;
	int nPos = 0;
	int ZCs = 0;

	int *Positives;
	
	Positives = (int*)malloc(num_data * sizeof(int));
	
	for (i = 0; i < num_data; i++) {
		if (data[i] > 0) {
			Positives[i] = 1;
		}
		else {
			Positives[i] = 0;
		}
	}

	for (i = 0; i < num_data-1; i++) {
		if (Positives[i + 1] - Positives[i] != 0) {
			ZCs++;
		}
	}

	free(Positives);
	return ZCs;
}

void differ(float *data, int num_data, float *diff) {
	int i;
	
	for (i = 0; i < num_data - 1; i++) {
		diff[i] = data[i + 1] - data[i];
	}
}

bool detectLeadOnOff(float *data, int num_data, int Fs, float threshold) {
	int i;
	int nZCs;
	float nZCsPerSec;

	float mean_data;
	float max_data;
	float base;
	float sum;
	float *diff;
	float *base_corrected;

	bool lead_onoff;

	diff = (float*)malloc((num_data - 1) * sizeof(float));
	base_corrected = (float*)malloc((num_data - 1) * sizeof(float));

	differ(data, num_data, diff);

	max_data = -1000;
	sum = 0;

	for (i = 0; i < num_data-1; i++) {
		sum = sum + diff[i];
		
			if (diff[i] > max_data) {
				max_data = diff[i];
			}
	}

	mean_data = sum / (num_data - 1);

	base = mean_data + (max_data - mean_data) / 4;

	for (i = 0; i < num_data - 1; i++) {
		base_corrected[i] = diff[i] - base;
	}

	nZCs = cntZeroCrossing(base_corrected, num_data - 1);

	nZCsPerSec = (float)(nZCs * Fs) / num_data;

	if (nZCsPerSec > threshold) {
		lead_onoff = false;
	}
	else {
		lead_onoff = analPeaks(data, num_data, BLKSIZE, NUM_PKS, THRESHOLD);
	}

	free(diff);
	free(base_corrected);

	return lead_onoff;
}

float PearsonCorrCoeff(float *templ, float *data, int num_data) {
	int i;
	float mean_templ, mean_data;
	float std_templ, std_data;
	
	float sum_templ, sum_data;
	float sq_templ, sq_data;
	float sum_cross;
	float mean_cross;

	float corrcoef;

	sum_templ = 0;
	sum_data = 0;
	sq_templ = 0;
	sq_data = 0;
	sum_cross = 0;

	for (i = 0; i < num_data; i++) {
		sum_templ = sum_templ + templ[i];
		sum_data = sum_data + data[i];
		sq_templ = sq_templ + (templ[i] * templ[i]);
		sq_data = sq_data + (data[i] * data[i]);
		sum_cross = sum_cross + (templ[i] * data[i]);
	}

	mean_templ = sum_templ / num_data;
	mean_data = sum_data / num_data;
	std_templ = sqrt((sq_templ / num_data)-(mean_templ*mean_templ));
	std_data = sqrt((sq_data / num_data)-(mean_data*mean_data));
	mean_cross = sum_cross / num_data;

	corrcoef = (mean_cross - mean_templ*mean_data) / (std_templ*std_data);

	return corrcoef;
}

bool analPeaks(float *data, int num_data, int blksize, int num_pks, float threshold) {
	int i;
	int j;
	int k;

	int sPtr, ePtr;
	float *templ;
	float *blk;
	float *maxVal;
	float *corr;
	int *loc;

	int *maxIdx;

	int N, cnt;
	int real_cnt;

	float mean_data, std_data;
	float sum_data, sq_data;
	float sum_corr;
	float corrcoef;

	N = (int)((num_data - (2 * OFFSET)) / blksize);

	blk = (float*)malloc(sizeof(float)*blksize);
	templ = (float*)malloc(sizeof(float)*blksize);	
	maxVal = (float*)malloc(sizeof(float)*N);
	maxIdx = (int*)malloc(sizeof(int)*N);

	sum_data = 0;
	sq_data = 0;
	sum_corr = 0;
	cnt = 0;

	for (i = 0; i < num_data; i++) {
		sum_data = sum_data + data[i];
		sq_data = sq_data + data[i] * data[i];
	}

	mean_data = sum_data / num_data;
	std_data = sqrt(sq_data / num_data - mean_data*mean_data);

	for (i = 0; i < N; i++) {
		sPtr = i*blksize + OFFSET;
		ePtr = i*blksize + OFFSET + blksize - 1;

		maxVal[i] = -1000;

		for (k = 0; k < blksize; k++) {
			if (data[sPtr + k] > maxVal[i]) {
				maxVal[i] = data[sPtr + k];
				maxIdx[i] = sPtr + k;
			}
		}
	}

	for (i = 0; i < N; i++) {
		if ((maxVal[i] > mean_data + std_data)) {
			if ((data[maxIdx[i]] - data[maxIdx[i] - 1])*(data[maxIdx[i] + 1] - data[maxIdx[i]]) <= 0) {
				for (k = 0; k < blksize; k++) {
					templ[k] = data[maxIdx[i] - (int)(TEMP_SIZE / 2) + k];
				}
				cnt++;
			}
		}
	}

	for (i = 0; i < blksize; i++) {
		templ[i] = templ[i] / cnt;
	}

	corr = (float*)malloc(sizeof(float)*cnt);
	loc = (int*)malloc(sizeof(int)*cnt);

	k = 0;

	for (i = 0; i < N; i++) {
		if (maxVal[i] > mean_data + std_data) {
			if ((data[maxIdx[i]] - data[maxIdx[i] - 1])*(data[maxIdx[i] + 1] - data[maxIdx[i]]) <= 0) {
				for (j = 0; j < blksize; j++) {
					blk[j] = data[maxIdx[i] - (int)(TEMP_SIZE / 2) + j];
				}

				corr[k] = PearsonCorrCoeff(templ, blk, blksize);
				loc[k] = maxIdx[i];
				k++;
			}
		}
	}

	if (cnt > num_pks) {
		real_cnt = num_pks;
	}
	else {
		real_cnt = cnt;
	}
	lead_quick_sort(corr, loc, 0, cnt - 1, 1);

	for (i = 0; i < real_cnt; i++) {
		sum_corr = sum_corr + corr[i];
	}

	corrcoef = sum_corr / real_cnt;

	free(blk);
	free(templ);
	free(maxVal);
	free(maxIdx);
	free(corr);
	free(loc);

	if (corrcoef > threshold) {
		return true;
	}
	else {
		return false;
	}

}

void lead_quick_sort(float* data, int* loc, int low, int high, int sort_type)
{
	//sort_type=0: ascend, sort_type=1: descend
	int p = 0;
	if (low >= high) return;

	p = lead_Partition(data, loc, low, high, sort_type);

	lead_quick_sort(data, loc, low, p - 1, sort_type);
	lead_quick_sort(data, loc, p + 1, high, sort_type);
}

void lead_swap(float *data, int *loc, int i, int j)
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

int lead_Partition(float *data, int *loc, int low, int high, int sort_type)
{
	int i;
	int p = low;
	for (i = p + 1; i <= high; ++i)
	{
		if (sort_type == 0)
		{
			if (data[i] < data[p])
			{
				lead_swap(data, loc, i, p);
				if (i != p + 1)
				{
					lead_swap(data, loc, i, p + 1);
				}
				p = p + 1;
			}
		}
		else
		{
			if (data[i] > data[p])
			{
				lead_swap(data, loc, i, p);
				if (i != p + 1)
				{
					lead_swap(data, loc, i, p + 1);
				}
				p = p + 1;
			}
		}
	}

	return p;
}