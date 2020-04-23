package com.samsung.slsi;

import java.util.ArrayList;

/**
 * Created by ch36.park on 2017. 2. 15..
 */

public interface HPatchTest {
    void runTest(int id, ArrayList<Object> params);

    void addSPatchTestObserver(HPatchTestObserver observer);
    void removeSPatchTestObserver(HPatchTestObserver observer);
}
