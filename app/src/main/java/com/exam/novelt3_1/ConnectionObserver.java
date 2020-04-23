package com.exam.novelt3_1;

public interface ConnectionObserver {
    void update(String patch_name, boolean isConnected, int batteryRatio);
}
