package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;
import com.samsung.slsi.hpatchdevice.StateContext;

/**
 * Created by ch36.park on 2017. 7. 19..
 */

public class FinalState extends HPatch3StateBase {
    public static final String name = "Final";

    FinalState(StateContext stateContext, HPatch3Device sPatch3Device) {
        super(name, stateContext, sPatch3Device);
    }

    @Override
    protected void onEnter() {
        log("Final State: " + device.getId());
        device.disconnect();
    }

    @Override
    protected void onExit() {

    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        device.disconnect();
    }
}
