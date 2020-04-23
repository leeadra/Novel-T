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
 * @file		BatteryCR2032.java
 * @brief		Battery (CR2032) Status Ratio Calculation
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/11/29
 *
 * <b>revision history :</b>
 * - 2017/02/02 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchdevice;

import static com.samsung.slsi.FileLogUtil.log;

public class BatteryCR2032 {
    private static final int X_INIT_STAGE = 5;

    private int xInitStage = 0;
    private double xSum = 0;

    private double x = 870.8930;
    private double P = 5;
    private final double R = 3591.318;

    private int ii = 0;

    private double preprocess(double input) {
        double Xp = x;
        double Pp = P;
        double K = Pp * 1 / (Pp + R);

        x = Xp + K * (input - Xp);
        P = Pp - K * Pp;

        ii++;
        if ((ii % 1000) == 0) {
            P = 5;
        }

        return x;
    }

    private double getGeneralModelSin4(double x) {
        final double A1 = 85.144588146993868;
        final double B1 = 0.008147947513532;
        final double C1 = 1.047470649348883;
        final double A2 = 35.392893942361873;
        final double B2 = 0.021766710392795;
        final double C2 = 1.393001542564400;
        final double A3 = 66.681947963946101;
        final double B3 = 0.064184391178445;
        final double C3 = -17.787753859736021;
        final double A4 = 64.853599889433369;
        final double B4 = 0.066103991034581;
        final double C4 = -3.373329723432995;

        return A1 * Math.sin(B1 * x + C1)
                + A2 * Math.sin(B2 * x + C2)
                + A3 * Math.sin(B3 * x + C3)
                + A4 * Math.sin(B4 * x + C4);
    }

    /* Soo-Yong Kim (odin.kim@samsung.com) 2017-02-08 18:27

    Initial value:
    x=870.8930
    P=5
    R= 3591.318
    For Loop (ii)
        Xp = x; Pp=P;
        K = Pp*1/(Pp+R)
        x=Xp + K*(input - Xp)
        P = Pp - K*Pp
        output = x
        if ((ii % 1000) == 0) P=5
    End Loop


    General model Sin4:
         f(x) =
                a1*sin(b1*x+c1) + a2*sin(b2*x+c2) + a3*sin(b3*x+c3) +
                a4*sin(b4*x+c4)
    Coefficients (with 95% confidence bounds):
    Goodness of fit:
      SSE: 5.552e+05
      R-square: 0.9826
      Adjusted R-square: 0.9826
    RMSE: 3.805

    A1 = 85.144588146993868
    B1 = 0.008147947513532
    C1 = 1.047470649348883
    A2 = 35.392893942361873
    B2 = 0.021766710392795
    C2 = 1.393001542564400
    A3 = 66.681947963946101
    B3 = 0.064184391178445
    C3 = -17.787753859736021
    A4 = 64.853599889433369
    B4 = 0.066103991034581
    C4 = -3.373329723432995
     */

    /****************************************************************************************
     * @return Battery level. 0 - 100%
     * ***************************************************************************************
     * Calculates battery level percentage for CR2032 batteries
     * by Soo-Yong Kim (odin.kim@samsung.com)
     * @param adc_sample  adc sample
     */
    public int getBatteryRatio(int adc_sample) {
        int batteryRatio = -1;

        if (adc_sample > 0) {
            if (xInitStage < X_INIT_STAGE) {
                xInitStage++;

                xSum += adc_sample;
                x = xSum / xInitStage;
            }

            batteryRatio = (int) getGeneralModelSin4(preprocess(adc_sample));

            if (batteryRatio > 100) {
                batteryRatio = 100;
            } else if (batteryRatio < 1) {
                batteryRatio = 1;
            }
        }

        //log("CR2032: " + adc_sample + " --> " + batteryRatio + " (" + x + ")");
        return batteryRatio;
    }
}