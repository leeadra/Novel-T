#include "stdafx.h"
#include "params.h"
#include <math.h>
#include <string.h>
#include <malloc.h>


void fft(float *x, int num_data, float *output, int nn, int isign)
{
	int n, mmax, m, j, istep, i;
	float wtemp, wr, wpr, wpi, wi, theta;
	float tempr, tempi;
	float *data;
	
	data = (float*)malloc(sizeof(float)*(2 * nn + 1));

	if (isign == 1) {
		for (i = 0; i < num_data; i++) {
			data[2 * i + 1] = x[i];
			data[2 * i + 2] = 0;
		}
		for (i = num_data; i < nn; i++) {
			data[2 * i + 1] = 0;
			data[2 * i + 2] = 0;
		}
	}

	else if (isign == -1) {
		memcpy(data, x, sizeof(float)*(2 * nn + 1));
	}

	n = nn << 1;
	j = 1;
	for (i = 1; i < n; i += 2) {
		if (j > i) {
			tempr = data[j];     
			data[j] = data[i];     
			data[i] = tempr;
			tempr = data[j + 1]; 
			data[j + 1] = data[i + 1]; 
			data[i + 1] = tempr;
		}
		m = n >> 1;
		while (m >= 2 && j > m) {
			j -= m;
			m >>= 1;
		}
		j += m;
	}

	mmax = 2;
	
	while (n > mmax) {
		istep = 2 * mmax;
		theta = TWOPI / (isign*mmax);
		wtemp = sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi = sin(theta);
		wr = 1.0;
		wi = 0.0;
		for (m = 1; m < mmax; m += 2) {
			for (i = m; i <= n; i += istep) {
				j = i + mmax;
				tempr = wr*data[j] - wi*data[j + 1];
				tempi = wr*data[j + 1] + wi*data[j];
				data[j] = data[i] - tempr;
				data[j + 1] = data[i + 1] - tempi;
				data[i] += tempr;
				data[i + 1] += tempi;
			}

			wr = (wtemp = wr)*wpr - wi*wpi + wr;
			wi = wi*wpr + wtemp*wpi + wi;
		}

		mmax = istep;
	}
	if (isign == 1) {
		memcpy(output, data, sizeof(float)*(2 * nn + 1));
	}
	else if (isign == -1) {
		for (i = 0; i < num_data; i++) {
			output[i] = data[2 * i + 1] / nn;
		}
	}
	free(data);
}