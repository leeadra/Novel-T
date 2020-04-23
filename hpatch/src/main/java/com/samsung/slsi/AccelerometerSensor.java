package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 6. 15..
 */

public interface AccelerometerSensor {
    interface Observer {
        void onAccelerometerUpdated(AccelerometerInformation information);
    }

    void addObserver(Observer observer);
    void removeObserver(Observer observer);
}
