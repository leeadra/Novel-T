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
 * @file		BLEPermissionManager.java
 * @brief		BLE Permission Manager for Android OS
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.1
 * @date		2016/12/2
 *
 * <b>revision history :</b>
 * - 2016/12/2 First creation
 *******************************************************************************
 */

package com.samsung.slsi.hpatchhost;

import android.Manifest;
import android.app.Activity;

class BLEPermissionManager {

    private PermissionManager manager;

    BLEPermissionManager(Activity activity) {
        manager = new PermissionManager(activity);

        manager.add(PermissionManager.REQUEST_ENABLE_BT,
                Manifest.permission.BLUETOOTH,
                "S-PATCH needs BLUETOOTH permission to communicate S-PATCH Device");
        manager.add(PermissionManager.REQUEST_ENABLE_BT_ADMIN,
                Manifest.permission.BLUETOOTH_ADMIN,
                "S-PATCH needs BLUETOOTH_ADMIN permission to communicate S-PATCH Device");
        manager.add(PermissionManager.REQUEST_ENABLE_ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                "S-PATCH needs ACCESS_COARSE_LOCATION permission to communicate S-PATCH Device");
        manager.add(PermissionManager.REQUEST_ENABLE_ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "S-PATCH needs ACCESS_FINE_LOCATION permission to communicate S-PATCH Device");
    }

    boolean isGranted() {
        return manager.isPermissionGranted();
    }

    void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        manager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
