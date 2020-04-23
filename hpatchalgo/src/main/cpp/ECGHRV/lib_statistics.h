

float mean_f32(float* src_data, int data_size);
float mean_int32(int* src_data, int data_size);
float min_f32(float* src_data, int data_size, int* min_index);
float max_f32(float* src_data, int data_size, int* max_index);
int max_int32(int* src_data, int data_size, int* max_index);
float median_f32(float* src_data, int data_size);
float std_dev_f32(float* src_data, int data_size);

void hist(float* src_data, int data_size, int bin_num, int* no, float* xo);