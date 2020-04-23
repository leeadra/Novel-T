package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketType;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;

public class DeviceInformationState extends HPatch3StateBase {
    public static final String name = "Device Information Query";

    DeviceInformationState(StateContext stateContext, HPatch3Device device) {
        super(name, stateContext, device);
    }

    @Override
    public void onEnter() {
        device.requestDeviceInformation();

        final int timeoutMilli = 5000;
        setTimeout(new Runnable() {
            @Override
            public void run() {
                chageState(name);
            }
        }, timeoutMilli);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        if (packet.getType() == HPatchPacketType.ReadResponse) {
            device.parseDeviceInformation(packet);

            chageState(ModeSettingState.name);
        }
    }
}
