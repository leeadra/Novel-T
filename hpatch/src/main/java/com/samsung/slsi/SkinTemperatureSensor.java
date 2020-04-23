package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 8. 16..
 */

public interface SkinTemperatureSensor {
    interface Observer {
        void onSkinTemperatureUpdated(SkinTemperatureInformation information);
    }

    void addObserver(Observer observer);
    void removeObserver(Observer observer);
}
