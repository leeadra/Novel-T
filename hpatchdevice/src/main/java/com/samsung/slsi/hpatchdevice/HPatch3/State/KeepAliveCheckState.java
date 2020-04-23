package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketType;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateContext;

public class KeepAliveCheckState extends HPatch3StateBase {
    public static final String name = "KeepAliveCheck";

    KeepAliveCheckState(StateContext stateContext, HPatch3Device device) {
        super(name, stateContext, device);
        myState = this;
    }

    private final State myState;

    @Override
    public void onEnter() {
        device.sendKeepAlive();

        final int timeoutMilli = 1000;
        setTimeout(new Runnable() {
            @Override
            public void run() {
                if (getCurrentState() == myState) {
                    chageState(name);
                }
            }
        }, timeoutMilli);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        //log("BLE: " + packet.getType());

        if (packet.getType() == HPatchPacketType.KeepAliveResponse) {
            chageState(DeviceInformationState.name);
        }
    }
}
