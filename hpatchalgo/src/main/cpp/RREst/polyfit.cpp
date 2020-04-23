#include "stdafx.h"
#include <math.h>
#include <stdlib.h>

void polyfit2(float* data, int num_data, float* fitted)
{
	float* x;
	int i;
	float D, Da, Db, Dc;
	float S40, S30, S20, S10, S00;
	float S21, S11, S01;
	float a[3];

	S00 = 0;
	S10 = 0;
	S20 = 0;
	S30 = 0;
	S40 = 0;
	S01 = 0;
	S11 = 0;
	S21 = 0;

	x = (float*)malloc(sizeof(float)*num_data);
	
	for (i = 0; i < num_data; i++) {
		x[i] = (float)i;
	}

	for (i = 0; i < num_data; i++) {
		S40 = S40 + (x[i] * x[i] * x[i] * x[i]);
		S30 = S30 + (x[i] * x[i] * x[i]);
		S20 = S20 + (x[i] * x[i]);
		S10 = S10 + x[i];
		S00 = (float)num_data;

		S01 = S01 + data[i];
		S11 = S11 + (x[i] * data[i]);
		S21 = S21 + (x[i] * x[i] * data[i]);
	}

	D = S40 * (S20*S00 - S10*S10) - S30 * (S30*S00 - S10*S20) + S20 * (S30*S10 - S20*S20);

	Da = S21 * (S20*S00 - S10*S10) - S11 * (S30*S00 - S10*S20) + S01 * (S30*S10 - S20*S20);

	Db = S40 * (S11*S00 - S01*S10) - S30 * (S21*S00 - S01*S20) + S20 * (S21*S10 - S11*S20);

	Dc = S40 * (S20*S01 - S10*S11) - S30 * (S30*S01 - S10*S21) + S20 * (S30*S11 - S20*S21);

	a[2] = Da / D;
	a[1] = Db / D;
	a[0] = Dc / D;

	for (i = 0; i < num_data; i++) {
		fitted[i] = a[2] * i*i + a[1] * i + a[0];
	}

	free(x);
}