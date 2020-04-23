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
 * @file		PermissionHelper.java
 * @brief		Permission Helper
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
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

public class PermissionHelper {

    public interface PermissionObserver {
        void onPermissionShowExplanation(int requestCode);
        void onPermissionResult(int requestCode, boolean isGranted);
    }

    private ArrayList<PermissionObserver> observers = new ArrayList<>();

    public void addObserver(PermissionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(PermissionObserver observer) {
        observers.remove(observer);
    }

    public boolean checkPermission(Activity activity, String targetPermission, int requestCode) {
        boolean isGranted = (ContextCompat.checkSelfPermission(activity, targetPermission)
                == PackageManager.PERMISSION_GRANTED);
        if (!isGranted) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, targetPermission)) {
                for (PermissionObserver observer : observers) {
                    observer.onPermissionShowExplanation(requestCode);
                }
            }

            ActivityCompat.requestPermissions(activity,
                    new String[]{targetPermission},
                    requestCode);
        }
        return isGranted;
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        boolean isGranted = (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED);

        for (PermissionObserver observer : observers) {
                observer.onPermissionResult(requestCode, isGranted);
        }
    }
}
