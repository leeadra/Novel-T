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
 * @file		HRVTest.java
 * @brief		HRV Algorithm Unit Test
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
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class HRVTest {
    @Test
    public void hrvTestData() {
        int reset = 0;
        int fs = 250;
        int update_5_min = 0;

        float expected_AVNN = 618.321838f;
        float expected_SDNN = 32.446499f;
        float expected_SDANN = 0.000000f;
        float expected_ASDNN = 0.000000f;
        float expected_NN50 = 2.000000f;
        float expected_pNN50 = 0.4192872f;
        float expected_RMSSD = 14.924599f;
        float expected_TINN = 102.322571f;
        float expected_VLF = 225.089508f;
        float expected_LF = 493.48843f;
        float expected_HF = 67.875282f;

        //TEST_DATA_SIZE 479
        int[] qrs_index_test_data = {
                147, 294, 438, 582, 733, 888, 1043, 1199, 1355, 1508, 1655, 1801, 1948, 2097, 2249, 2407, 2562, 2722, 2884, 3050
                , 3214, 3371, 3527, 3680, 3830, 3982, 4130, 4276, 4417, 4554, 4690, 4825, 4961, 5100, 5247, 5396, 5549, 5712, 5886, 6061
                , 6239, 6412, 6576, 6742, 6902, 7060, 7212, 7364, 7518, 7676, 7841, 8002, 8161, 8325, 8489, 8652, 8813, 8969, 9125, 9280
                , 9435, 9590, 9744, 9906, 10070, 10235, 10402, 10563, 10724, 10886, 11047, 11207, 11362, 11516, 11670, 11826, 11984, 12146, 12305, 12460
                , 12617, 12777, 12938, 13102, 13259, 13417, 13579, 13741, 13904, 14061, 14217, 14373, 14528, 14682, 14835, 14984, 15138, 15292, 15447, 15604
                , 15757, 15910, 16067, 16229, 16394, 16556, 16715, 16873, 17034, 17192, 17348, 17503, 17654, 17810, 17967, 18124, 18283, 18439, 18593, 18748
                , 18905, 19063, 19218, 19373, 19527, 20096, 20242, 20391, 20540, 20696, 20853, 21014, 21169, 21323, 21479, 21632, 21788, 21948, 22106, 22264
                , 22418, 22574, 22731, 22894, 23057, 23215, 23375, 23534, 23695, 23860, 24020, 24180, 24344, 24505, 24661, 24814, 24961, 25105, 25251, 25398
                , 25551, 25701, 25856, 26013, 26171, 26329, 26483, 26640, 26796, 26949, 27099, 27254, 27409, 27567, 27727, 27883, 28045, 28205, 28365, 28519
                , 28677, 28836, 28995, 29148, 29303, 29461, 29620, 29781, 29936, 30095, 30253, 30412, 30565, 30719, 30877, 31036, 31193, 31346, 31505, 31663
                , 31819, 31974, 32131, 32290, 32446, 32600, 32757, 32912, 33069, 33220, 33373, 33529, 33684, 33843, 33995, 34149, 34303, 34456, 34610, 34760
                , 34913, 35070, 35233, 35401, 35570, 35729, 35890, 36050, 36207, 36363, 36514, 36667, 36823, 36981, 37140, 37295, 37457, 37618, 37784, 37945
                , 38102, 38261, 38423, 38584, 38744, 38904, 39068, 39232, 39399, 39559, 39720, 39884, 40050, 40212, 40368, 40522, 40678, 40837, 40996, 41151
                , 41301, 41450, 41594, 41732, 41865, 41996, 42126, 42259, 42393, 42540, 42694, 42871, 43053, 43245, 43436, 43616, 43801, 43982, 44163, 44345
                , 44510, 44671, 44826, 44981, 45134, 45285, 45883, 46029, 46180, 46331, 46488, 46644, 46800, 46953, 47108, 47261, 47414, 47561, 47706, 47852
                , 48002, 48156, 48314, 48473, 48628, 48787, 48946, 49107, 49263, 49414, 49566, 49718, 49872, 50026, 50176, 50327, 50480, 50637, 50800, 50959
                , 51116, 51274, 51434, 51592, 51743, 51892, 52038, 52187, 52340, 52492, 52645, 52799, 52953, 53110, 53262, 53412, 53565, 53720, 53879, 54044
                , 54211, 54371, 54537, 54702, 54868, 55029, 55186, 55348, 55506, 55666, 55826, 55983, 56143, 56303, 56460, 56615, 56768, 56922, 57076, 57231
                , 57381, 57533, 57683, 57833, 57982, 58127, 58274, 58422, 58572, 58723, 58870, 59017, 59169, 59319, 59469, 59616, 59761, 60055, 60202, 60354
                , 60509, 60667, 60826, 60981, 61142, 61297, 61450, 61599, 61746, 61894, 62046, 62201, 62356, 62509, 62664, 62819, 62973, 63127, 63276, 63426
                , 63578, 63733, 63888, 64043, 64203, 64359, 64516, 64674, 64832, 64990, 65141, 65285, 65430, 65572, 65716, 65863, 66014, 66165, 66317, 66470
                , 66624, 66777, 66925, 67074, 67226, 67380, 67533, 67677, 67820, 67961, 68098, 68238, 68382, 68528, 68678, 68825, 68977, 69119, 69259, 69397
                , 69534, 69668, 69804, 69943, 70080, 70218, 70354, 70491, 70629, 70767, 70904, 71041, 71179, 71319, 71459, 71600, 71742, 71885, 72029, 72177
                , 72324, 72471, 72616, 72765, 72911, 73056, 73201, 73346, 73491, 73638, 73786, 73934, 74084, 74236, 74388, 74539, 74693, 74845, 74997
        };


        float[] actual_results = HPatchAlgorithmWrapper.getECGHeartRateVariability(
                reset,
                qrs_index_test_data,
                qrs_index_test_data.length,
                fs,
                update_5_min);
        float[] expected_results = {
                expected_AVNN,
                expected_SDNN,
                expected_SDANN,
                expected_ASDNN,
                expected_NN50,
                expected_pNN50,
                expected_RMSSD,
                expected_TINN,
                expected_VLF,
                expected_LF,
                expected_HF
        };

        for (int i = 0; i < expected_results.length; i++) {
            assertEquals("" + i + " Item", expected_results[i], actual_results[i]);
        }
    }

    @Test
    public void hrv20150101131742_DataTest() {
        int reset = 1;
        int fs = 256;
        int update_5_min = 1;

        //HRV: 2015-01-01 13:17:42.553, TT: 00:05:00, Reset: 1, QRS-Count: 200, FS: 256, 5Min: true
        int[] qrs_index_test_data = {
            449, 640, 831, 1023, 1214, 1405, 1596, 1787, 1979, 2170, 2361, 2552, 2744, 2935, 3126, 3317,
                    3508, 3700, 3891, 4082, 4273, 4464, 4656, 4847, 5038, 5229, 5421, 5612, 5803, 5994, 6185, 6377,
                    6568, 6759, 6950, 7141, 7333, 7524, 7715, 7906, 8098, 8289, 8480, 8671, 8862, 9054, 9245, 9436,
                    9627, 9819, 10010, 10201, 10392, 10583, 10775, 10966, 11157, 11348, 11539, 11731, 11922, 12113, 12304, 12496,
                    12687, 12878, 13069, 13260, 13452, 13643, 13834, 14025, 14216, 14408, 14599, 14790, 14981, 15173, 15364, 15555,
                    15746, 15937, 16129, 16320, 16511, 16702, 16894, 17085, 17276, 17467, 17658, 17850, 18041, 18232, 18423, 18614,
                    18806, 18997, 19188, 19379, 19571, 19762, 19953, 20144, 20335, 20527, 20718, 20909, 21100, 21291, 21483, 21674,
                    21865, 22056, 22248, 22439, 22630, 22821, 23012, 23204, 23395, 23586, 23777, 23968, 24160, 24351, 24542, 24733,
                    24925, 25116, 25307, 25498, 25689, 25881, 26072, 26263, 26454, 26646, 26837, 27028, 27219, 27410, 27602, 27793,
                    27984, 28175, 28366, 28558, 28749, 28940, 29131, 29323, 29514, 29705, 29896, 30087, 30279, 30470, 30661, 30852,
                    31043, 31235, 31426, 31617, 31808, 32000, 32191, 32382, 32573, 32764, 32956, 33147, 33338, 33529, 33721, 33912,
                    34103, 34294, 34485, 34677, 34868, 35059, 35250, 35441, 35633, 35824, 36015, 36206, 36398, 36589, 36780, 36971,
                    37162, 37354, 37545, 37736, 37927, 38118, 38310, 38501,
        };

        float expected_AVNN = 746.9378f;
        float expected_SDNN = 1.6117501f;
        float expected_SDANN = 0.000000f;
        float expected_ASDNN = 0.000000f;
        float expected_NN50 = 0.000000f;
        float expected_pNN50 = 0.0f;
        float expected_RMSSD = 2.5744038f;
        float expected_TINN = 0.0f;
        float expected_VLF = 3.489076E-4f;
        float expected_LF = 0.0152830435f;
        float expected_HF = 0.99374497f;

        float[] actual_results = HPatchAlgorithmWrapper.getECGHeartRateVariability(
                reset,
                qrs_index_test_data,
                qrs_index_test_data.length,
                fs,
                update_5_min);

        for (int i = 0; i < actual_results.length; i++) {
            Log.d("HRV", "" + i + " Item : " + actual_results[i]);
        }

        float[] expected_results = {
                expected_AVNN,
                expected_SDNN,
                expected_SDANN,
                expected_ASDNN,
                expected_NN50,
                expected_pNN50,
                expected_RMSSD,
                expected_TINN,
                expected_VLF,
                expected_LF,
                expected_HF
        };

        for (int i = 0; i < expected_results.length; i++) {
            assertEquals("" + i + " Item", expected_results[i], actual_results[i]);
        }
    }
}