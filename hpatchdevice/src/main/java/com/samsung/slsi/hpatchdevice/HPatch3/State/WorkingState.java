package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;
import com.samsung.slsi.hpatchdevice.StateContext;

public class WorkingState extends HPatch3StateBase {
    public static final String name = "Working";

    WorkingState(StateContext stateContext, HPatch3Device sPatch3Device) {
        super(name, stateContext, sPatch3Device);
    }

    final int timeoutMilli = 60000;
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            log("WorkingState Timeout!!!");
            device.sendReset();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            device.disconnect();
        }
    };

    @Override
    public void onEnter() {
        setTimeout(timeoutRunnable, timeoutMilli);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        cancelTimeout();
        setTimeout(timeoutRunnable, timeoutMilli);

        device.parsePacket(packet);
    }
}
