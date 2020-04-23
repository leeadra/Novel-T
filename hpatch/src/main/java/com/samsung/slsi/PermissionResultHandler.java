package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 7. 24..
 */

public interface PermissionResultHandler {
    void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults);
}
