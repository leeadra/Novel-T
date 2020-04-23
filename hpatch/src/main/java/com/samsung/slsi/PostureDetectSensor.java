package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 6. 13..
 */

public interface PostureDetectSensor {
    void addObserver(PostureDetectObserver observer);
    void removeObserver(PostureDetectObserver observer);
}
