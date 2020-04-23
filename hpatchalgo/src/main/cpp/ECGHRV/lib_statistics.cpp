#include "lib_statistics.h"
#include "lib_data.h"
#include <math.h>
#include <malloc.h>
#include <string.h>

#define EPS 2.2204e-16
float mean_f32(float* src_data, int data_size)
{
	int index = 0;
	float sum = 0;
	float mean_val = 0;

	for (index = 0; index < data_size; index++)
	{
		sum += src_data[index];
	}

	mean_val = sum / data_size;

	return mean_val;
}

float mean_int32(int* src_data, int data_size)
{
	int index = 0;
	float sum = 0;
	float mean_val = 0;

	for (index = 0; index < data_size; index++)
	{
		sum += (float)src_data[index];
	}

	mean_val = sum / data_size;

	return mean_val;
}

float std_dev_f32(float* src_data, int data_size)
{
	int index = 0;
	float mean_val = 0;
	float sum_val = 0;
	float result;

	mean_val = mean_f32(src_data, data_size);

	for (index = 0; index < data_size; index++)
	{
		sum_val += (src_data[index] - mean_val) * (src_data[index] - mean_val);
	}
	sum_val = sum_val / (data_size-1);

	result = sqrt(sum_val);

	return result;
}
float min_f32(float* src_data, int data_size, int* min_index)
{
	int index;
	float min_val = src_data[0];
	*min_index = 0;

	for (index = 1; index < data_size; index++)
	{
		if (min_val > src_data[index])
		{
			min_val = src_data[index];
			*min_index = index;
		}
	}

	return min_val;
}


float max_f32(float* src_data, int data_size, int* max_index)
{
	int index;
	float max_val = src_data[0];
	*max_index = 0;

	for (index = 1; index < data_size; index++)
	{
		if (max_val < src_data[index])
		{
			max_val = src_data[index];
			*max_index = index;
		}
	}

	return max_val;
}

int max_int32(int* src_data, int data_size, int* max_index)
{
	int index;
	int max_val = src_data[0];
	*max_index = 0;

	for (index = 1; index < data_size; index++)
	{
		if (max_val < src_data[index])
		{
			max_val = src_data[index];
			*max_index = index;
		}
	}

	return max_val;
}

float median_f32(float* src_data, int data_size)
{

	ecg_quick_sort(src_data, NULL, 0, data_size - 1, 0);

	if (data_size % 2 == 0) {
		// if there is an even number of elements, return mean of the two elements in the middle
		return((src_data[data_size / 2] + src_data[data_size / 2 - 1]) / 2.0);
	}
	else {
		// else return the element in the middle
		return src_data[data_size / 2];
	}

}

void hist(float* src_data, int data_size, int bin_num, int* no, float* xo)
{
	float miny, maxy;
	int min_index, max_index;
	float bin_width = 0;
	int index = 0;
	int data_index = 0;
	float* xx;
	float* x;
	float* bins;
	int* nn;

	xx = (float*)malloc(sizeof(float)*(bin_num + 1));
	bins = (float*)malloc(sizeof(float)*(bin_num + 1));
	nn = (int*)malloc(sizeof(int)*(bin_num + 2));
	x = (float*)malloc(sizeof(float)*bin_num);

	memset(nn, 0, sizeof(float)*(bin_num + 2));

	miny = min_f32(src_data, data_size, &min_index);
	maxy = max_f32(src_data, data_size, &max_index);

	if (miny == maxy)
	{
		miny = miny - floor((float)bin_num / 2.0f) - 0.5f;
		maxy = maxy - ceil((float)bin_num / 2.0f) - 0.5f;
	}

	bin_width = (maxy - miny) / bin_num;

	for (index = 0; index < (bin_num + 1); index++)
	{
		xx[index] = miny + bin_width * index;
	}
	xx[bin_num] = maxy;
	for (index = 0; index < bin_num; index++)
	{
		x[index] = xx[index] + (bin_width/2.0f);
	}

	for (index = 0; index < (bin_num + 1); index++)
	{
		bins[index] = xx[index] + EPS;
	}

	for (data_index = 0; data_index < data_size; data_index++)
	{
		if (src_data[data_index] <= bins[0])
		{
			nn[0] = nn[0] + 1;
		}
	}
	for (index = 1; index < (bin_num + 2); index++)
	{
		for (data_index = 0; data_index < data_size; data_index++)
		{
			if (src_data[data_index] > bins[index - 1] && src_data[data_index] <= bins[index])
			{
				nn[index] = nn[index] + 1;
			}
		}
	}
	
	nn[1] = nn[0] + nn[1];
	nn[bin_num] = nn[bin_num + 1] + nn[bin_num];

	memcpy(no, nn + 1, sizeof(float)*bin_num);
	memcpy(xo, x, sizeof(float)*bin_num);

	free(xx);
	free(x);
	free(bins);
	free(nn);
}