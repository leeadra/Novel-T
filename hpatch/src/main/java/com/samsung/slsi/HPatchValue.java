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
 * @file		HPatchValue.java
 * @brief		HPatch Value interface
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		1.0
 * @date		2016/11/22
 *
 * <b>revision history :</b>
 * - 2016/11/22 First creation
 *******************************************************************************
 */

package com.samsung.slsi;

public interface HPatchValue {
    String getName();

    String getValue();
    int getValueAsInteger();
    float getValueAsFloat();
    double getValueAsDouble();
    boolean getValueAsBoolean();

    void setValue(String value);
    void setValue(int value);
    void setValue(float value);
    void setValue(double value);
    void setValue(boolean value);

    HPatchValueType getValueType();
}
