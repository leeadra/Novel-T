package com.samsung.slsi.hpatchalgo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ch36.park on 2017. 7. 10..
 */
public class ECGFitness {
    private long handle;

    public enum Gender {
        Female(0),
        Male(1);

        ///////////////////////////////
        // Methods for value setting

        private final int value;
        Gender(int value) {
            this.value = value;
        }
        public int getValue() { return value; }

        private static Map<Integer, Gender> map = new HashMap<>();
        static {
            for (Gender type : Gender.values()) {
                map.put(type.value, type);
            }
        }
        public static Gender valueOf(int type) {
            return map.get(type);
        }
    }

    public ECGFitness() throws Exception {
        handle = HPatchAlgorithmWrapper.ecgFitnessCreateFitinfo();
        if (handle == 0) {
            throw new Exception("Fail to create Fit-Info");
        }
    }

    public void close() {
        if (handle != 0) {
            HPatchAlgorithmWrapper.ecgFitnessDestroyFitinfo(handle);
            handle = 0;
        }
    }

    public int getHRMax(int hr) {
        return HPatchAlgorithmWrapper.ecgFitnessGetHRmax(handle, hr);
    }

    public int getHRRest(int hr) {
        return HPatchAlgorithmWrapper.ecgFitnessGetHRrest(handle, hr);
    }

    /***
     *
     * @param hr
     * @param age
     * @return pct HR
     */
    public float getPCTHR(int hr, int age) {
        return HPatchAlgorithmWrapper.ecgFitnessGetpctHR(handle, hr, age);
    }

    /***
     * @return SportsZone 1 ~ 5
     */
    public int getSportsZone() {
        return HPatchAlgorithmWrapper.ecgFitnessGetSportsZone(handle);
    }

    public float getMaxAvailableHR(int age) {
        return HPatchAlgorithmWrapper.ecgFitnessGetMaxAvailableHR(age);
    }

    public float getEnergyExpenditureKiloCalorie(float avgHR, int age, Gender gender, int w_kg, float dur_min) {
        return HPatchAlgorithmWrapper.ecgFitnessGetEnergyExpenditure(handle, avgHR, age, gender.getValue(), w_kg, dur_min);
    }
}
