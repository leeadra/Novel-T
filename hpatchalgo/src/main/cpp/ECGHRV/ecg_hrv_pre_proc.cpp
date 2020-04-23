#include <malloc.h>
#include <string.h>

#include "lib_statistics.h"
void get_rri(int* qrs_index, int fs, int data_size, float* rri)
{
	int index;

	for (index = 1; index < data_size; index++)
	{
		rri[index - 1] = (float)(qrs_index[index] - qrs_index[index - 1]) / (float)fs * 1000.0f;

	}
}

void ectopic_beat_removal(float* rri, int data_size, float* rri_e)
{
	const int WIN_SIZE = 20;
	const int REMOVE_WIN_SIZE = 4;

	int index;
	int sum_count = 0;
	int r_index;
	int* outliers;

	float left_avg, right_avg, avg_val, sum;

	outliers = (int*)malloc(sizeof(int)*data_size);

	memcpy(rri_e, rri, sizeof(float)*data_size);

	//Get Outliers
	memset(outliers, 0, sizeof(float)*data_size);
	for (index = WIN_SIZE; index < (data_size - WIN_SIZE); index++)
	{
		left_avg = mean_f32(rri + index - WIN_SIZE, WIN_SIZE);
		right_avg = mean_f32(rri + index + 1, WIN_SIZE);
		avg_val = (left_avg + right_avg) / 2.0f;

		if (rri[index] < 200 || rri[index] > 4000)
		{
			outliers[index] = 1;			
		}
		else if (rri[index] > (avg_val*1.2) || rri[index] < (avg_val*0.8))
		{
			outliers[index] = 1;
		}

	}


	//remove ectopic beats
	for (index = REMOVE_WIN_SIZE; index < (data_size - REMOVE_WIN_SIZE); index++)
	{
		if (outliers[index] == 1)
		{
			sum_count = 0;
			sum = 0;

			for (r_index = index - REMOVE_WIN_SIZE; r_index < (index + REMOVE_WIN_SIZE + 1); r_index++)
			{
				if (outliers[r_index] == 0)
				{
					sum += rri[r_index];
					sum_count++;
				}
			}

			if (sum == 0)
			{
				rri_e[index] = rri_e[index - 1];
			}
			else
			{
				rri_e[index] = sum / sum_count;
			}
		}
	}



	free(outliers);
}