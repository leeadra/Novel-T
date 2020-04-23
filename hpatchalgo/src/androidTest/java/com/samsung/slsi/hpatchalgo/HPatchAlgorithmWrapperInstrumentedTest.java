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
 * @file		HPatchAlgorithmWrapperInstrumentedTest.java
 * @brief		SPatch Algorithm Unit Test
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import static com.samsung.slsi.hpatchalgo.HPatchAlgorithmWrapper.getRespirationRateEstimation;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class HPatchAlgorithmWrapperInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.samsung.slsi.spatchalgo.test", appContext.getPackageName());
    }

    @Test
    public void respiratoryRateEstimation_call() throws Exception {
        final float expectedRespiratoryRateEstimation = 5.625f;
        final int testCount = 100;

        int[] Ridx;
        float[] QRSminmax;
        int num_data;
        int secWin;

        Ridx = new int[testCount];
        QRSminmax = new float[testCount];
        num_data = testCount;
        secWin = 32;

        float actualRespiratoryRateEstimation = getRespirationRateEstimation(Ridx, QRSminmax, num_data, secWin);

        assertEquals(expectedRespiratoryRateEstimation, actualRespiratoryRateEstimation);
    }

    @Test
    public void respiratoryRateEstimation_RPA_tv_RSA_FileTest() {
        int secStep = 3;    // 3 sec
        int secWin = 32;    // 32 sec

        final int FS = 300; //Sample Rate
        final int RE_FS = 4;

        final int sampleSize = 814;

        float[] RPA_t;
        float[] RPA_v;
        int[] RSA_idx;

        RPA_t = readFloatFile("RPA_t.bin");
        assertThat(RPA_t, notNullValue());
        Assert.assertEquals(sampleSize, RPA_t.length);
        RPA_v = readFloatFile("RPA_v.bin");
        assertThat(RPA_v, notNullValue());
        Assert.assertEquals(sampleSize, RPA_v.length);
        RSA_idx = readIntFile("RSA.bin");
        assertThat(RSA_idx, notNullValue());
        Assert.assertEquals(sampleSize, RSA_idx.length);

        float[] RSA_t = new float[RSA_idx.length];
        for (int i = 0; i < sampleSize; i++) {
            RSA_t[i] = (float)RSA_idx[i] / FS;
        }

        int N = 149;
        float[] BrPMs;

        BrPMs = readFloatFile("BrPM.bin");
        assertThat(BrPMs, notNullValue());
        assertThat(BrPMs.length, is(N));

        int currIdx = 0;
        for (int i = 0; i < N; i++) {
            int sTime = i * secStep;
            int eTime = i * secStep + secWin;
            int[] idxCrop = new int[2];
            int Init = (currIdx++ == 0) ? 1 : 0;

            // Input RPA data (time, value)
            rangeFinder(RPA_t, sampleSize, currIdx, sTime, eTime, idxCrop, Init);

            float[] CroppedRPA_t = cropDatafloat(RPA_t, idxCrop[0], idxCrop[1]);
            float[] CroppedRPA_v = cropDatafloat(RPA_v, idxCrop[0], idxCrop[1]);

            int numDataWin = idxCrop[1] - idxCrop[0] + 1;

            for (int cnt = 0; cnt < numDataWin; cnt++) {
                CroppedRPA_t[cnt] = CroppedRPA_t[cnt] - (float)sTime;
            }

            // Input RSA data (indices)
            rangeFinder(RSA_t, sampleSize, currIdx, sTime, eTime, idxCrop, Init);

            int[] CroppedRSA = cropDataint(RSA_idx, sTime, FS, idxCrop[0], idxCrop[1]);

            numDataWin = idxCrop[1] - idxCrop[0] + 1;

            float BrPM = rrEst(CroppedRSA,  CroppedRPA_v, numDataWin, secWin);

            assertThat("" + i + "-th is " + BrPMs[i], BrPM, is(BrPMs[i]));
        }
    }

    @Test
    public void respiratoryRateEstimation_RREst_Test() {
        for (int i = 0; i < 149; i++) {
            String fileName = String.format(Locale.getDefault(), "RREst_Test/RREst_%03d.txt", i);
            RREst_Test test = readRREst_Test(fileName);
            assertThat("Fail to read " + i + "-th RREst_XXX.txt file", test, notNullValue());

            float BrPM = rrEst(test.CroppedRSA,  test.CroppedRPA_v, test.numDataWin, test.secWin);

            System.out.println("" + i + "th: Expected: " + test.expectedBrPM + ", Actual: " + BrPM);
            assertThat("" + i + "-th must be " + test.expectedBrPM, BrPM, is(test.expectedBrPM));
        }
    }

    @Test
    public void respiratoryRateEstimation_RRE_Test() {
        for (int i = 0; i < 159; i++) {
            String fileName = String.format(Locale.getDefault(), "RRE_test/RR_%d.txt", i);
            RREst_Test test = readRREst_Test(fileName);
            assertThat("Fail to read " + i + "-th RREst_XXX.txt file", test, notNullValue());

            float BrPM = rrEst(test.CroppedRSA,  test.CroppedRPA_v, test.numDataWin, test.secWin);

            System.out.println("" + i + "th: Expected: " + test.expectedBrPM + ", Actual: " + BrPM);
            assertThat("" + i + "-th must be " + test.expectedBrPM, BrPM, is(test.expectedBrPM));
        }
    }

    private float rrEst(int[] rsa_idx, float[] croppedRPA_v, int numDataWin, int secWin) {
        return getRespirationRateEstimation(rsa_idx, croppedRPA_v, numDataWin, secWin);
    }

    class RREst_Test {
        int numDataWin;
        int[] CroppedRSA;
        float[] CroppedRPA_v;
        int secWin;
        float expectedBrPM;
    }

    //@TargetApi(Build.VERSION_CODES.KITKAT)
    private RREst_Test readRREst_Test(String fileName) {
        RREst_Test test = new RREst_Test();

        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = testContext.getAssets();
        try {
            InputStream inputStream = assetManager.open(fileName);
            Scanner scanner = new Scanner(inputStream);

            test.numDataWin = scanner.nextInt();
            test.secWin = scanner.nextInt();

            test.expectedBrPM = scanner.nextFloat();

            test.CroppedRSA = new int[test.numDataWin];
            test.CroppedRPA_v = new float[test.numDataWin];

            for (int i = 0; i < test.numDataWin; i++) {
                test.CroppedRSA[i] = scanner.nextInt();
                test.CroppedRPA_v[i] = scanner.nextFloat();
            }
        } catch (IOException e) {
            e.printStackTrace();
            test = null;
        }
        return test;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private float[] readFloatFile(String fileName) {
        ArrayList<Float> floats = new ArrayList<>();
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = testContext.getAssets();
        try {
            InputStream inputStream = assetManager.open(fileName);
            ByteBuffer byteBuffer = ByteBuffer.wrap(inputStreamToByteArray(inputStream))
                    .order(ByteOrder.LITTLE_ENDIAN);

            int position = 0;
            while(byteBuffer.capacity() - position >= 4) {
                floats.add(byteBuffer.getFloat(position));
                position += 4;
            }
        } catch (IOException e) {
            e.printStackTrace();
            floats = null;
        }
        int size = 0;
        if (floats != null) {
            size = floats.size();
        }
        float[] primitiveFloats = new float[size];
        for (int i = 0; i < size; i++) {
            primitiveFloats[i] = floats.get(i);
        }
        return primitiveFloats;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private int[] readIntFile(String fileName) {
        ArrayList<Integer> values = new ArrayList<>();
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = testContext.getAssets();
        try {
            InputStream inputStream = assetManager.open(fileName);
            ByteBuffer byteBuffer = ByteBuffer.wrap(inputStreamToByteArray(inputStream))
                    .order(ByteOrder.LITTLE_ENDIAN);

            int position = 0;
            while(byteBuffer.capacity() - position >= 4) {
                values.add(byteBuffer.getInt(position));
                position += 4;
            }
        } catch (IOException e) {
            e.printStackTrace();
            values = null;
        }
        int size = 0;
        if (values != null) {
            size = values.size();
        }
        int[] primitiveValues = new int[size];
        for (int i = 0; i < size; i++) {
            primitiveValues[i] = values.get(i);
        }
        return primitiveValues;
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) {
        byte[] resultBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int read = -1;
        try {
            while ( (read = inputStream.read(buffer)) != -1 ) {
                byteArrayOutputStream.write(buffer, 0, read);
            }

            resultBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return resultBytes;
    }

    private static void rangeFinder(float[] data, int num_data, int currIdx, int onset, int offset, int[] output, int init)
    {
        int i;

        if (currIdx >= num_data) {
            Assert.fail("Invalid Data!!\n");
        }

        else {
            for (i = currIdx+1; i < num_data; i++) {

                if ((data[i - 1] <= (float)onset) & (data[i] >= (float)onset)) {
                    output[0] = i;
                }
                else if (init == 1) {
                    output[0] = 0;
                }

                if ((data[i - 1] <= (float)offset)&(data[i] >= (float)offset)) {
                    output[1] = i - 1;
                    break;
                }
            }
        }

        if ((output[0] == -1) | (output[1] == -1)) {
            Assert.fail("Invalid Data!!\n");
        }
    }

    private static float[] cropDatafloat(float[] data, int sIdx, int eIdx)
    {
        int i;
        int num_data;

        num_data = eIdx - sIdx + 1;
        float[] Cropped = new float[num_data];
        for (i = 0; i < num_data; i++) {
            Cropped[i] = data[sIdx + i];
        }
        return Cropped;
    }

    private static int[] cropDataint(int[] data, int sTime, int Fs, int sIdx, int eIdx)
    {
        int i;
        int num_data;

        num_data = eIdx - sIdx + 1;
        int[] Cropped = new int[num_data];
        for (i = 0; i < num_data; i++) {
            Cropped[i] = data[sIdx + i] - sTime*Fs;
        }
        return Cropped;
    }
}
