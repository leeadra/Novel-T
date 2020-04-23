package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketType;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;

public class RegistrationState extends HPatch3StateBase {
    public static final String name = "Registration";

    RegistrationState(StateContext stateContext, HPatch3Device sPatch3Device) {
        super(name, stateContext, sPatch3Device);
    }

    @Override
    public void onEnter() {
        device.sendHostKey();

        final int timeoutMilli = 5000;
        setTimeout(new Runnable() {
            @Override
            public void run() {
                //log("Timeout " + name + " state");
                chageState(name);
            }
        }, timeoutMilli);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        if (packet.getType() == HPatchPacketType.ResponseTransferKey) {
            try {
                byte[] encryptedTransferKey = packet.getPayload();

                device.setEncryptedTransferKey(encryptedTransferKey);

                chageState(StartState.name);
            } catch (Exception e) {
                //log(e.getLocalizedMessage());
                finalState();
            }
        }
    }
}
