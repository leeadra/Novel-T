#include "lib_data.h"
//#include <windows.h>
#include <malloc.h>

void ecgSwap(float *data, int *loc, int i, int j)
{
	int tmp_loc;
	float tmp = data[i];
	data[i] = data[j];
	data[j] = tmp;
	if (loc != 0)
	{
		tmp_loc = loc[i];
		loc[i] = loc[j];
		loc[j] = tmp_loc;
	}
}

int ecgPartition(float* data, int* loc, int low, int high, int sort_type)
{
	int i;
	int p = low;
	for (i = p + 1; i <= high; ++i)
	{
		if (sort_type == 0)
		{
			if (data[i] < data[p])
			{
                ecgSwap(data, loc, i, p);
				if (i != p + 1)
				{
                    ecgSwap(data, loc, i, p + 1);
				}
				p = p + 1;
			}
		}
		else
		{
			if (data[i] > data[p])
			{
                ecgSwap(data, loc, i, p);
				if (i != p + 1)
				{
                    ecgSwap(data, loc, i, p + 1);
				}
				p = p + 1;
			}
		}
	}

	return p;
}
void ecg_quick_sort(float *data, int *loc, int low, int high, int sort_type)
{
	//sort_type=0: ascend, sort_type=1: descend
	int p = 0;
	if (low >= high) return;

	p = ecgPartition(data, loc, low, high, sort_type);

    ecg_quick_sort(data, loc, low, p - 1, sort_type);
    ecg_quick_sort(data, loc, p + 1, high, sort_type);
}

void linear_intp(float* in_t, float* in_v, int in_size, float* out_v, int fs, int out_size)
{
	int in_index, out_index;
	float Ts = 1 / (float)fs;
	float curr_t = 0;
	int curr_in_index=0;
	for (out_index = 0; out_index < out_size; out_index++)
	{
		if (curr_t < in_t[0])
		{
			out_v[out_index] = in_v[0];
		}
		else if (curr_t > in_t[in_size-1])
		{
			out_v[out_index] = in_v[in_size - 1];
		}
		else
		{
			while (1)
			{
				if (curr_t >= in_t[curr_in_index] && curr_t <= in_t[curr_in_index + 1])
				{
					out_v[out_index] = (in_v[curr_in_index + 1] - in_v[curr_in_index])*(curr_t - in_t[curr_in_index]) / (in_t[curr_in_index + 1] - in_t[curr_in_index]) + in_v[curr_in_index];


					break;
				}
				else
				{
					curr_in_index++;
				}
			}
		}
		curr_t += Ts;
	}
}

void tridiag_gen(float* a, float* b, float* c, float*d, int len)
{
	int i;
	float b_f;
	float *f;

	f = (float*)malloc(sizeof(float)*len);

	//Gauss elimination: forward substitution
	b_f = b[0];
	d[0] = d[0] / b_f;
	for (i = 1; i < len; i++)
	{
		f[i] = c[i - 1] / b_f;
		b_f = b[i] - a[i] * f[i];
		if (b_f == 0)
		{
			free(f);
			return;
		}
		d[i] = (d[i] - d[i - 1] * a[i]) / b_f;
	}

	//backsubstitution
	for (i = len - 2; i >= 0; i--)
	{
		d[i] -= (d[i + 1] * f[i + 1]);
	}

	free(f);
}

void getYD_gen(float* x, float* y, float* yd, int len)
{
	int i;
	float h0, h1, r0, r1;
	float* a;
	float* b;
	float* c;

	//allocate memory for tridiagonal bands A, B, C
	a = (float*)malloc(sizeof(float)*len);
	b = (float*)malloc(sizeof(float)*len);
	c = (float*)malloc(sizeof(float)*len);

	//init first row data
	h0 = x[1] - x[0];
	h1 = x[2] - x[1];
	r0 = (y[1] - y[0]) / h0;
	r1 = (y[2] - y[1]) / h1;
	b[0] = h1 * (h0 + h1);
	c[0] = (h0 + h1)*(h0 + h1);
	yd[0] = r0 * (3 * h0*h1 + 2 * h1*h1) + r1*h0*h0;

	//init tridiagonal bands a, b, c and column vector yd
	//yd will later be used to return the derivatives
	for (i = 1; i < len - 1; i++)
	{
		h0 = x[i] - x[i - 1];
		h1 = x[i + 1] - x[i];
		r0 = (y[i] - y[i - 1]) / h0;
		r1 = (y[i + 1] - y[i]) / h1;
		a[i] = h1;
		b[i] = 2 * (h0 + h1);
		c[i] = h0;
		yd[i] = 3 * (r0*h1 + r1*h0);
	}

	//last row
	a[i] = (h0 + h1)*(h0 + h1);
	b[i] = h0*(h0 + h1);
	yd[i] = r0*h1*h1 + r1*(3 * h0*h1 + 2 * h0*h0);

	//solve for the tridiagonal matrix: yd = yd*inv(tridiag matrix)
	tridiag_gen(a, b, c, yd, len);

	free(a);
	free(b);
	free(c);
}

void spline(float* x1, float* y1, int len1, float* x2, float* y2, int len2)
{
	int i, j;
	float* yd;
	float a0, a1, a2, a3, x, dx, dy, p1, p2, p3;

	//error checking
	if (x2[0] < x1[0] || x2[len2 - 1] > x1[len1 - 1])
	{
		return;
	}

	//compute 1st derivatives at each point
	yd = (float*)malloc(sizeof(float)*len1);
	getYD_gen( x1,y1,yd, len1);
	//p1 is left endpoint of interval
	//p2 is resampling position
	//p3 is right endpoint of interval
	//j is input index of current interval

	p3 = x2[0] - 1;
	for (i = j = 0; i < len2; i++)
	{
		//check if in new interval
		p2 = x2[i];

		if (p2 > p3)
		{
			//find the interval which contains p2
			for (; j<len1&&p2>x1[j]; j++);
			if (p2 < x1[j])
			{
				j--;
			}
			p1 = x1[j]; //update left endpoint
			p3 = x1[j + 1]; //update right endpoint

			//compute spline coefficients
			dx = 1.0f / (x1[j + 1] - x1[j]);
			dy = (y1[j + 1] - y1[j]) * dx;
			a0 = y1[j];
			a1 = yd[j];
			a2 = dx*(3.0f*dy - 2.0f*yd[j] - yd[j + 1]);
			a3 = dx*dx*(-2.0f*dy + yd[j] + yd[j + 1]);
		}

		//use Horner's rule to calculate cubic polynomial
		x = p2 - p1;
		y2[i] = ((a3*x + a2)*x + a1)*x + a0;


		
	}
	free(yd);

}
