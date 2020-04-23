package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateContext;

public class ReadyState extends OTAState {
	public static final String name = "Ready";

	ReadyState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		if (device.readyBPFW()) {
			chageState(StartState.name);
		} else {
			finalState();
		}
	}

	@Override
	protected void onExit() {
	}

	@Override
	protected void onBPOTAResponse(byte result, byte[] payload) {
	}
}
