package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 6. 13..
 */

public interface FallDetectInformation {
    byte[] getRawData();

    boolean isFall();
    FallType getFallType();
}
