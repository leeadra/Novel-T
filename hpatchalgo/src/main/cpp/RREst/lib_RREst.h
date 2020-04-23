void linearInt(float *data_t, float *data_v, int num_data, float *output, int Fs, int secWin);

void SSA(float *data, int num_data, int maxGroupNum, float *output, int minTh);

void linearIntRSA(int *data, int num_data, float *output, int Fs, int REFS, int secWin);

float rrEst(int *Ridx, float *QRSminmax, int num_data, int secWin);

void detrending(float *data, int num_data, float *detrended);

float calcBrPM(float *data, int num_data, int nfft, int tot_group, int target_num, float *output);