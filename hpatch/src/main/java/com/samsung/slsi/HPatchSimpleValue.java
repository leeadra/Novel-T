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
 * @file		HPatchSimpleValue.java
 * @brief		HPatch Simple Value Implementation
 *              All Data is stored as string
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2016/11/29
 *
 * <b>revision history :</b>
 * - 2016/11/29 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

import java.text.NumberFormat;
import java.text.ParseException;

public class HPatchSimpleValue implements HPatchValue {

    private String name;
    private String stringValue;
    private HPatchValueType type;

    public HPatchSimpleValue(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return stringValue;
    }

    @Override
    public int getValueAsInteger() {
        int n;
        try {
            Number number = NumberFormat.getNumberInstance().parse(stringValue);
            n = number.intValue();
        } catch (ParseException e) {
            //e.printStackTrace();
            n = 0;
        }
        return n;
    }

    @Override
    public float getValueAsFloat() {
        float n;
        try {
            Number number = NumberFormat.getNumberInstance().parse(stringValue);
            n = number.floatValue();
        } catch (ParseException e) {
            //e.printStackTrace();
            n = 0;
        }
        return n;
    }

    @Override
    public double getValueAsDouble() {
        double n;
        try {
            Number number = NumberFormat.getNumberInstance().parse(stringValue);
            n = number.doubleValue();
        } catch (ParseException e) {
            //e.printStackTrace();
            n = 0;
        }
        return n;
    }

    @Override
    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(stringValue);
    }

    @Override
    public void setValue(String value) {
        stringValue = value;
        type = HPatchValueType.SPatchString;
    }

    @Override
    public void setValue(int value) {
        stringValue = "" + value;
        type = HPatchValueType.SPatchInteger;
    }

    @Override
    public void setValue(float value) {
        stringValue = "" + value;
        type = HPatchValueType.SPatchFloat;
    }

    @Override
    public void setValue(double value) {
        stringValue = "" + value;
        type = HPatchValueType.SPatchDouble;
    }

    @Override
    public void setValue(boolean value) {
        stringValue = "" + value;
        type = HPatchValueType.SPatchBoolean;
    }

    @Override
    public HPatchValueType getValueType() {
        return type;
    }
}
