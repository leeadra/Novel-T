package com.samsung.slsi.hpatchdevice.HPatch3;

import com.samsung.slsi.SkinTemperatureInformation;

/**
 * Created by ch36.park on 2017. 8. 16..
 */

public class HPatch3SkinTemperatureInformation implements SkinTemperatureInformation {
    private float skinTemperature;

    HPatch3SkinTemperatureInformation(float skinTemperature) {
        this.skinTemperature = skinTemperature;
    }

    @Override
    public float getSkinTemperature() {
        return skinTemperature;
    }
}
