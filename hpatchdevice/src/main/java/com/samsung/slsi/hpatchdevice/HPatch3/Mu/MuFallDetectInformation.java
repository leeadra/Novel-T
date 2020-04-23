package com.samsung.slsi.hpatchdevice.HPatch3.Mu;

import com.samsung.slsi.FallDetectInformation;
import com.samsung.slsi.FallType;


/**
 * Created by ch36.park on 2017. 6. 26..
 */

public class MuFallDetectInformation implements FallDetectInformation {
    private byte[] rawData;

    private boolean isFall;
    private FallType fallType;

    public MuFallDetectInformation(boolean isFall, FallType fallType) {
        this.isFall = isFall;
        this.fallType = fallType;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    @Override
    public byte[] getRawData() {
        return rawData;
    }

    @Override
    public boolean isFall() {
        return isFall;
    }

    @Override
    public FallType getFallType() {
        return fallType;
    }
}
