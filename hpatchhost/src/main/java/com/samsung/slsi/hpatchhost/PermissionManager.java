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
 * @file		PermissionManager.java
 * @brief		Permission Manager
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

import android.app.Activity;
import android.widget.Toast;

import java.util.HashMap;

public class PermissionManager implements PermissionHelper.PermissionObserver {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_ENABLE_BT_ADMIN = 2;
    public static final int REQUEST_ENABLE_WRITE_EXTERNAL_STORAGE = 3;
    public static final int REQUEST_ENABLE_READ_EXTERNAL_STORAGE = 4;
    public static final int REQUEST_ENABLE_ACCESS_FINE_LOCATION = 5;
    public static final int REQUEST_ENABLE_ACCESS_COARSE_LOCATION = 6;
    public static final int REQUEST_ENABLE_INTERNET = 7;

    private Activity activity;
    private PermissionHelper permissionHelper = new PermissionHelper();

    class PermissionItem {
        int requestCode;
        String permissionText;
        String explanation;

        boolean isGranted;

        PermissionItem(int requestCode, String permissionText, String explanation) {
            this.requestCode = requestCode;
            this.permissionText = permissionText;
            this.explanation = explanation;
        }
    }

    private HashMap<Integer, PermissionItem> requestCodePermissionItemMap = new HashMap<>();


    public PermissionManager(Activity activity) {
        this.activity = activity;
        permissionHelper.addObserver(this);
    }

    public void add(int requestCode, String permission, String explanation) {
        requestCodePermissionItemMap.put(requestCode,
                new PermissionItem(requestCode, permission, explanation));
    }

    public boolean isPermissionGranted() {
        for (int requestCode : requestCodePermissionItemMap.keySet()) {
            PermissionItem item = requestCodePermissionItemMap.get(requestCode);
            if (!permissionHelper.checkPermission(activity, item.permissionText, item.requestCode)) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionShowExplanation(int requestCode) {
        PermissionItem item = requestCodePermissionItemMap.get(requestCode);
        if (item != null) {
            Toast.makeText(
                    activity.getApplicationContext(),
                    item.explanation,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onPermissionResult(int requestCode, boolean isGranted) {
        PermissionItem item = requestCodePermissionItemMap.get(requestCode);
        if (item != null) {
            item.isGranted = isGranted;
        }
    }
}
