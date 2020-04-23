package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketType;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;

public class StartState extends HPatch3StateBase {
    public static final String name = "Start";

	StartState(StateContext stateContext, HPatch3Device sPatch3Device) {
		super(name, stateContext, sPatch3Device);
	}

    @Override
    public void onEnter() {
        device.requestECGStart();

        final int timeoutMilli = 3000;
        setTimeout(new Runnable() {
            @Override
            public void run() {
                log(name + " Timeout");
                chageState(name);
            }
        }, timeoutMilli);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        device.parsePacket(packet);

        if (packet.getType() == HPatchPacketType.Composite) {
            cancelTimeout();
            chageState(WorkingState.name);
        }
    }
}
