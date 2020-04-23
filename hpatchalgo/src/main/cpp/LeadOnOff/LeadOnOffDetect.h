int cntZeroCrossing(float *data, int num_data);
void differ(float *data, int num_data, float *diff);
bool detectLeadOnOff(float *data, int num_data, int Fs, float threshold);
float PearsonCorrCoeff(float *templ, float *data, int num_data);
bool analPeaks(float *data, int num_data, int blksize, int num_pks, float threshold);
int peak_detector(float* src_data, int data_size, int interval_size, float lowerFreq, float upperFreq, int Fs, int max_peak_num, float* peak_val, int* peak_index);
void lead_quick_sort(float* data, int* loc, int low, int high, int sort_type);
void lead_swap(float* data, int* loc, int i, int j);
int lead_Partition(float* data, int* loc, int low, int high, int sort_type);