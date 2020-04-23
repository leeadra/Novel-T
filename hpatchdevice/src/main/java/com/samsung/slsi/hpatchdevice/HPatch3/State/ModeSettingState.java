package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;
import com.samsung.slsi.hpatchdevice.StateContext;

public class ModeSettingState extends HPatch3StateBase {
    public static final String name = "Mode Setting";

    ModeSettingState(StateContext stateContext, HPatch3Device device) {
        super(name, stateContext, device);
    }

    @Override
    public void onEnter() {
        if (device.isNeedToSetMode()) {
            device.requestModeSetting();
            final int timeoutMilli = 5000;
            setTimeout(new Runnable() {
                @Override
                public void run() {
                    chageState(name);
                }
            }, timeoutMilli);
        } else {
            moveToNextState();
        }
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        if (device.parseModeSetting(packet)) {
            cancelTimeout();
            moveToNextState();
        }
    }

    private void moveToNextState() {
        chageState(RegistrationState.name);
    }
}
