#include "ECGFitness_proc.h"
#include <stdlib.h>


#ifndef NULL
#define NULL		(0)
#endif


Fit_info* create_Fitinfo()
{
	Fit_info *p;

	p = (Fit_info*)malloc(sizeof(Fit_info));

	p->HRmax = 0;
	p->HRrest = 1000;
	p->sportsZone = 1;
	p->pctHR = 0;
	p->burnedCalories = 0;

	return p;
}

void destroy_Fitinfo(Fit_info* p)
{
	if (p != NULL)
	{
		delete p;
	}
}

int getHRmax(Fit_info *fitInfo, int HRcurrent)
{
	if (HRcurrent > fitInfo->HRmax) {
		fitInfo->HRmax = HRcurrent;
	}
	return fitInfo->HRmax;
}

int getHRrest(Fit_info *fitInfo, int HRcurrent)
{
	if (HRcurrent != 0) {
		if (fitInfo->HRrest > HRcurrent) {
			fitInfo->HRrest = HRcurrent;
		}
	}
	return fitInfo->HRrest;
}

float getpctHR(Fit_info *fitInfo, int HRcurrent, int age)
{
	int maxHR = getMaxAvailableHR(age);
	fitInfo->pctHR = (float)HRcurrent * 100 / maxHR;
	return fitInfo->pctHR;
}

int getSportsZone(Fit_info *fitInfo)
{
	int HRzone;

	if (fitInfo->pctHR < 60)
		HRzone = 1;
	else if ((fitInfo->pctHR >= 60)&(fitInfo->pctHR < 70)) {
		HRzone = 2;
	}
	else if ((fitInfo->pctHR >= 70)&(fitInfo->pctHR < 80)) {
		HRzone = 3;
	}
	else if ((fitInfo->pctHR >= 80)&(fitInfo->pctHR < 90)) {
		HRzone = 4;
	}
	else if (fitInfo->pctHR >= 90) {
		HRzone = 5;
	}
	else {
		HRzone = -1;
	}

	fitInfo->sportsZone = HRzone;
	return fitInfo->sportsZone;
}

float getMaxAvailableHR(int age)
{
	float maximalHR;
	maximalHR = 209 - 0.7f * (float)age;
	return maximalHR;
}

float getEnergyExpenditure(Fit_info *fitInfo, float avgHR, int age, int gender, int w_kg, float dur_min)
{
	//-----------------------------------------------------------------------------
	// avgHR : average HR within the exercise duration
	// age : age
	// gender : male 1, female 0
	// w_kg : weights in kilogram
	// dur_min : exercise duration in minutes
	// output : return estimation result in kilo calories
	//-----------------------------------------------------------------------------

	float burnedCalories;

	getpctHR(fitInfo, (int)avgHR, age);

	if (fitInfo->pctHR > 60) {
		if (gender == 1) {
			burnedCalories = ((-55.0969f + (0.6309f * avgHR) + (0.1988f * (float)w_kg) + (0.2017f * (float)age)) / 4.184f) * dur_min;
		}
		else /*if (gender == 0) */ {
			burnedCalories = ((-20.4022f + (0.4472f * avgHR) - (0.1263f * (float)w_kg) + (0.074f * (float)age)) / 4.184f) * dur_min;
		}
	}
	else {
		burnedCalories = 0;
	}

	fitInfo->burnedCalories += burnedCalories;
	return fitInfo->burnedCalories;
}