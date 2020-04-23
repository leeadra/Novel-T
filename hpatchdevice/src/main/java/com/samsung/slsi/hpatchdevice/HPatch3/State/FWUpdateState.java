package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;
import com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.OTAState;
import com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.OTAStateFactory;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateFactory;

public class FWUpdateState extends HPatch3StateBase {
    public static final String name = "FW-Update";

    private OTAState state;
    private final StateContext fwUpdateStateContext = new StateContext() {
        @Override
        public State getCurrentState() {
            return state;
        }

        @Override
        public void changeState(String name) {
            try {
                if (state != null) {
                    state.exit();
                }

                State s = stateFactory.createState(name);
                if (s instanceof OTAState) {
                    state = (OTAState) s;
                    state.enter();
                } else {
                    log("Invalid OTA State: " + name);
                    state = null;
                }
            } catch (Exception e) {
                log(e.getLocalizedMessage());
            }
        }

        @Override
        public void finalState() {
            exitState();
        }
    };

    private void exitState() {
        chageState(KeepAliveCheckState.name);
    }

    private final StateFactory stateFactory = new OTAStateFactory(fwUpdateStateContext, device);

    FWUpdateState(StateContext stateContext, HPatch3Device sPatch3Device) {
        super(name, stateContext, sPatch3Device);
    }

    @Override
    public void onEnter() {
        fwUpdateStateContext.changeState(OTAStateFactory.DefaultState);
    }

    @Override
    public void onExit() {
        device.stopBPOTA();
    }

    @Override
    public void onPacketReceived(HPatch3Packet packet) {
        state.onPacketReceived(packet);
    }
}
