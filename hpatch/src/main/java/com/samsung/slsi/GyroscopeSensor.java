package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 6. 15..
 */

public interface GyroscopeSensor {
    interface Observer {
        void onGyroscopeUpdated(GyroscopeInformation information);
    }

    void addObserver(Observer observer);
    void removeObserver(Observer observer);
}
