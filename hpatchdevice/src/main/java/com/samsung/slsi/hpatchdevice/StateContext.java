package com.samsung.slsi.hpatchdevice;

public interface StateContext {
    State getCurrentState();
    void changeState(String name);
    void finalState();
}
