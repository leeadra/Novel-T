package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 6. 21..
 */

public interface ECGSaveObserver {
    void onECGFileCreated(String path);
    void onECGFileUpdated(String path);
    void onECGFileStored(String path);
}
