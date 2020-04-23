package com.samsung.slsi.hpatchdevice.HPatch3.Mu;

import com.samsung.slsi.PostureDetectInformation;
import com.samsung.slsi.PostureType;

/**
 * Created by ch36.park on 2017. 6. 26..
 */

public class MuPostureDetectInformation implements PostureDetectInformation {
    private byte[] rawData;

    private PostureType postureType;

    public MuPostureDetectInformation(PostureType postureType) {
        this.postureType = postureType;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    @Override
    public byte[] getRawData() {
        return rawData;
    }

    @Override
    public PostureType getPostureType() {
        return postureType;
    }
}
