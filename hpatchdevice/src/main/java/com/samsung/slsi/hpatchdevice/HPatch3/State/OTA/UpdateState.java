package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA;

import com.samsung.slsi.FileLogUtil;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP.BPOTAState;
import com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP.BPOTAStateFactory;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.StateFactory;

import java.util.Locale;

public class UpdateState extends OTAState {
	public static final String name = "Update";

	private BPOTAState state;
	private final StateContext updateStateContext = new StateContext() {
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
				if (s instanceof BPOTAState) {
					state = (BPOTAState) s;
					state.enter();
				} else {
					log("Invalid BP-OTA State: " + name);
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
        chageState(StopState.name);
	}

	private final StateFactory stateFactory = new BPOTAStateFactory(updateStateContext, device);

	UpdateState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		updateStateContext.changeState(BPOTAStateFactory.DefaultState);
	}


	@Override
	protected void onExit() {
	}

	@Override
	protected void onBPOTAResponse(byte result, byte[] payload) {
		if (payload.length >= 15) {
			byte cmd = payload[7];
			byte addr = payload[8];
			int data = ((int) payload[9] & 0xFF)
					| (((int)payload[10] & 0xFF) << 8);

            if (FileLogUtil.isInteralRelease) {
				String txt = "Receive: ";
				for (byte b : payload) {
					txt += String.format(Locale.getDefault(), "%02X", b);
				}
				device.logOTA(txt);
			}
			state.onBPBootROMPacket(result, cmd, addr, data, payload);
		}
	}
}
