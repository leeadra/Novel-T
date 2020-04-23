package com.samsung.slsi.hpatchdevice;

public interface State {
    String getName();

    void enter();
    void exit();
}
