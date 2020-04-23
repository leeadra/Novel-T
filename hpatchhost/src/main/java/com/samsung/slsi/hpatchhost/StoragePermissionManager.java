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
 * @file		StoragePermissionManager.java
 * @brief		Android Storage Permission Manager
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

class StoragePermissionManager {

    private PermissionManager writePermissionManager;
    private PermissionManager readPermissionManager;

    StoragePermissionManager(Activity activity) {
        writePermissionManager = new PermissionManager(activity);
        writePermissionManager.add(PermissionManager.REQUEST_ENABLE_WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                "WRITE_EXTERNAL_STORAGE permission is needed for logging and exporting!");

        readPermissionManager = new PermissionManager(activity);
        readPermissionManager.add(PermissionManager.REQUEST_ENABLE_READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                "READ_EXTERNAL_STORAGE permission is needed for importing!");
    }

    boolean isWriteGranted() {
        return writePermissionManager.isPermissionGranted();
    }
    boolean isReadGranted() {
        return readPermissionManager.isPermissionGranted();
    }

    void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        writePermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        readPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
