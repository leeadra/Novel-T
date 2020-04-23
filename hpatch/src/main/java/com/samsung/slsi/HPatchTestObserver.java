package com.samsung.slsi;

/**
 * Created by ch36.park on 2017. 2. 15..
 */
public interface HPatchTestObserver {
    void onTestResult(HPatchTest test, int id, HPatchValueContainer params);
}
