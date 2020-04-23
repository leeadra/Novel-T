#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "hanning.h"

void rangeFinder(float* data, int num_data, int currIdx, int onset, int offset, int* output, int init);

void cropDatafloat(float* data, int sIdx, int eIdx, float* Cropped);

void cropDataint(int * data, int sTime, int Fs, int sIdx, int eIdx, int * Cropped);

void dbg_ExportFile(char *fname, float* data, int num_data);

void normalize(float *data, int num_data, float *normalized);

void convFFT(float* sig1, float* sig2, int nn, float *output);

int peak_detector(float* src_data, int data_size, int interval_size, float lowerFreq, float upperFreq, int Fs, int max_peak_num, float* peak_val, int* peak_index);

void quick_sort(float *data, int *loc, int low, int high, int sort_type);

void swap(float *data, int *loc, int i, int j);

int Partition(float *data, int *loc, int low, int high, int sort_type);

void normalize_freq(float *data, int nn, float scale, float *normalized);

void windowing(float *data, int num_data, float* output);