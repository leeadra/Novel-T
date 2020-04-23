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
 * @file		HPatchSimpleValueContainer.java
 * @brief		HPatch Simple Value Container Implementation
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

import java.util.ArrayList;
import java.util.HashMap;

public class HPatchSimpleValueContainer implements HPatchValueContainer {

    private String name;
    private HashMap<String, HPatchValue> nameValueMap = new HashMap<>();
    private ArrayList<HPatchValue> valueArray = new ArrayList<>();

    public HPatchSimpleValueContainer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCount() {
        return valueArray.size();
    }

    @Override
    public HPatchValue getValue(int index) {
        if (index >= 0 && index < valueArray.size()) {
            return valueArray.get(index);
        } else {
            return null;
        }
    }

    @Override
    public HPatchValue getValue(String name) {
        if (nameValueMap.containsKey(name)) {
            return nameValueMap.get(name);
        } else {
            return null;
        }
    }

    @Override
    public HPatchValue setValue(String name) {
        if (nameValueMap.containsKey(name)) {
            return nameValueMap.get(name);
        } else {
            HPatchValue value = new HPatchSimpleValue(name);
            valueArray.add(value);
            nameValueMap.put(name, value);
            return value;
        }
    }
}
