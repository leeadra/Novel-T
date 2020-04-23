struct Fit_info {
	int HRmax;
	int HRrest;
	int sportsZone;
	float pctHR;
	float burnedCalories;
};


Fit_info* create_Fitinfo();
void destroy_Fitinfo(Fit_info* p);
int getHRmax(Fit_info *fitInfo, int HRcurrent);
int getHRrest(Fit_info *fitInfo, int HRcurrent);
float getpctHR(Fit_info *fitInfo, int HRcurrent, int age);
int getSportsZone(Fit_info *fitInfo);
float getMaxAvailableHR(int age);
float getEnergyExpenditure(Fit_info *fitInfo, float avgHR, int age, int gender, int w_kg, float dur_min);
