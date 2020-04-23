package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 2. 17..
 */

public interface HPatchBLEHeartRateObserver {
    void onHeartRateMeasurement(HPatchDeviceBLEInfo hPatchDeviceBLEInfo, int heartRate);
}
