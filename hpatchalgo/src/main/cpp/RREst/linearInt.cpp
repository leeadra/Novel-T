#include "stdafx.h"
#include <math.h>
#include <stdlib.h>

void linearInt(float* data_t, float* data_v, int num_data, float* output, int Fs, int secWin)
{
	int i, k, ix;
	int nSize;
	float* t;

	t = (float*)malloc(sizeof(float)*num_data);
	nSize = Fs * secWin;

	for (i = 0; i < num_data; i++) {
		t[i] = data_t[i] * Fs;
	}
	
	ix = 0;

	for (k = 0; k < nSize; k++) {

		if (ix < num_data) {
			if ((k < t[ix])&(ix == 0)) {
				output[k] = -1;
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
			output[k] = -1;
		}
		else {
			output[k] = -1;
		}
	}

	free(t);
}