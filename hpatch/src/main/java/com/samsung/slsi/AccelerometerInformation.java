package com.samsung.slsi;

import java.util.List;

/**
 * Created by ch36.park on 2017. 6. 15..
 */

public interface AccelerometerInformation {
    int getSequenceNumber();

    List<Integer> getXValues();
    List<Integer> getYValues();
    List<Integer> getZValues();
}
