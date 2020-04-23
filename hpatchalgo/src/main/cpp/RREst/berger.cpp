#include "stdafx.h"
#include "berger.h"
#include <stdlib.h>


void berger(int* data, int num_data, float* output, int num_output, int win_size, int Fs)
{
	// data : RRI data
	// num_data : the number of RRI data
	// win_size : window size for berger interpolation
	// Fs : sampling rate of original data

	int i, n;
	int N;
	int IX, ix, CONT;
	int cnt;

	float* data_ms;
	float sum;
	float mean;
	float max;

	sum = 0;
	mean = 0;
	max = 0;
	cnt = 0;

	N = win_size * RE_FS;

	data_ms = (float*)malloc((num_data + 1) * sizeof(float));
	data_ms[num_data] = -1;

	for (i = 0; i < num_data; i++) {
		data_ms[i] = RE_FS * (float)data[i] / Fs;
	}

	IX = 0;

	for (i = 0; i < num_output; i++) {
		output[i] = 0;
	}
	output[N - 1] = -1;

	for (n = 0; n < N-1; n++) {

		while (data_ms[IX + 1] < n - 1) {
			IX = IX + 1;
		}

		ix = IX + 1;
		CONT = 1;

		while (CONT) {
			// 1st case
			if ((float)n <= data_ms[0]) {
				output[n] = -1;
			}
			else if ((data_ms[ix - 1] >= (n - 1))&(data_ms[ix] < (n + 1))) {
				output[n] = output[n] + 1;
			}
			// 2nd case
			else if ((data_ms[ix - 1] < (n - 1))&(data_ms[ix] < (n + 1))) {
				output[n] = output[n] + (data_ms[ix] - (n - 1)) / (data_ms[ix] - data_ms[ix - 1]);
			}
			// 3rd case
			else if ((data_ms[ix - 1] > (n - 1))&(data_ms[ix] > (n + 1))) {
				output[n] = output[n] + ((n + 1) - data_ms[ix - 1]) / ((data_ms[ix] - data_ms[ix - 1]));
			}
			// 4th case
			else if ((data_ms[ix - 1] <= (n - 1))&(data_ms[ix] >= (n + 1))) {
				output[n] = output[n] + 2 / (data_ms[ix] - data_ms[ix - 1]);
			}
			else if (data_ms[ix] == -1) {
				output[n] = -1;
			}
			else {
				output[n] = -1;
			}

			if (data_ms[ix] < (n + 1)) {
				ix = ix + 1;
			}
			else {
				CONT = 0;
			}
		}
	}

	max = data_ms[num_data - 1];

	for (i = 0; i < N; i++) {
		if (output[i] > 0) {
			sum = sum + output[i];
			cnt++;
		}
	}

	mean = sum / cnt;

	for (i = 0; i < N; i++) {
		if (output[i] < 0) {
			output[i] = ((2 * max) / N / RE_FS) / mean;
		}
		else {
			output[i] = ((2 * max) / N / RE_FS) / output[i];
		}
	}

	free(data_ms);
}