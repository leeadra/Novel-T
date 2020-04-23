/*
 * Sensor Product Development Team, System LSI division.
 * Copyright (c) 2014-2017 Samsung Electronics, Inc.
 * All right reserved.
 *
 * This software is the confidential and proprietary information
 * of Samsung Electronics, Inc. (Confidential Information). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Samsung Electronics.
*/
/**
 *******************************************************************************
 * @file		RRETest.java
 * @brief		RRE Algorithm Unit Test
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2017/1/9
 *
 * <b>revision history :</b>
 * - 2017/1/9 First creation
 *******************************************************************************
 */


package com.samsung.slsi.hpatchalgo;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.samsung.slsi.hpatchalgo.HPatchAlgorithmWrapper.getRespirationRateEstimation;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RRETest {

    @Test
    public void rre_20170106123557_Test() {
        //2017-01-06 12:35:57.446
        //Count: 47, secWin: 64
        int[] Ridx = {
                0, 173, 347, 521, 695, 869, 1043, 1217, 1391, 1564, 1738, 1912, 2086, 2260, 2434, 2607, 2781,
                2955, 3129, 3303, 3477, 3651, 3824, 3998, 4172, 4346, 4520, 4694, 4868, 5041, 5215, 5389, 5563,
                5737, 5911, 6085, 6258, 6432, 6606, 6780, 6954, 7128, 7302, 7475, 7649, 7823, 7997,
        };
        float[] QRSminmax = {
                818.81323f, 818.33435f, 818.9981f, 818.3161f, 818.56476f, 818.27527f, 818.6898f, 818.6117f, 818.73254f, 818.4846f, 818.4487f, 818.6408f, 818.33264f, 817.9993f, 818.58545f, 818.97003f, 818.5456f,
                818.25055f, 818.26965f, 818.0187f, 818.64417f, 818.6661f, 819.1254f, 819.0852f, 818.28094f, 818.3103f, 818.2891f, 818.38416f, 818.4337f, 818.87665f, 818.7949f, 818.59863f, 818.9048f,
                818.6868f, 818.5655f, 818.2013f, 818.4275f, 818.5991f, 818.4303f, 818.4636f, 817.84125f, 818.69727f, 818.1966f, 818.8424f, 818.68396f, 818.84875f, 818.8262f,
        };

        int num_data = 47;
        int secWin = 64;

        float expectedRespiratoryRateEstimation = 12.1875f;

        float actualRespiratoryRateEstimation = getRespirationRateEstimation(Ridx, QRSminmax, num_data, secWin);
        assertEquals(expectedRespiratoryRateEstimation, actualRespiratoryRateEstimation);
    }

    @Test
    public void rre_20160125234928_Test() {
        //2016-01-25 23:49:28.324
        // Count: 64, secWin: 64
        int[] Ridx = {
            0, 178, 906, 1103, 1306, 1499, 1869, 2035, 2508, 2668, 2849, 3040, 3227, 3415, 3612, 3812,
            3998, 4182, 4368, 4551, 4741, 4980, 5114, 5306, 5554, 5883, 5967, 6090, 6277, 6365, 6692, 6897,
            7105, 7302, 7490, 7687, 7879, 8072, 8260, 8462, 8667, 8863, 9060, 9263, 9461, 9653, 9840, 10040,
            10240, 10439, 10633, 10841, 11049, 11261, 11454, 11652, 11845, 12038, 12221, 12423, 12628, 12839, 13036, 13245,
        };
        float[] QRSminmax = {
            952.3496f, 913.75726f, 896.64935f, 884.4352f, 952.39f, 923.4183f, 948.0364f, 919.94476f, 975.3657f, 922.7976f, 899.3801f, 897.2758f, 911.6853f, 903.34357f, 908.0206f, 909.7501f,
            929.568f, 922.1214f, 909.1917f, 912.0126f, 926.68414f, 1026.1174f, 912.2823f, 925.45996f, 963.327f, 848.3767f, 1069.7819f, 986.6549f, 1001.76465f, 951.80994f, 1010.3834f, 887.9505f,
            889.8086f, 944.1383f, 907.1757f, 894.8207f, 903.35675f, 890.1574f, 922.6507f, 899.6076f, 907.36035f, 907.0968f, 905.84467f, 900.05725f, 912.48914f, 930.7974f, 922.061f, 901.1189f,
            904.8432f, 932.1931f, 943.9379f, 897.9335f, 900.29034f, 913.2451f, 929.2461f, 901.0612f, 906.0466f, 899.7842f, 929.0093f, 892.0295f, 895.6438f, 911.373f, 931.12506f, 898.4888f,
        };

        int num_data = Ridx.length;
        int secWin = 64;

        float expectedRespiratoryRateEstimation = 13.125f;

        float actualRespiratoryRateEstimation = getRespirationRateEstimation(Ridx, QRSminmax, num_data, secWin);
        assertEquals(expectedRespiratoryRateEstimation, actualRespiratoryRateEstimation);
    }
}