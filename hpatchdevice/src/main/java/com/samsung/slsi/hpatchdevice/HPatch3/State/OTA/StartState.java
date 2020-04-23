package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA;

import com.samsung.slsi.hpatchdevice.HPatch3.BPOTAPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateContext;

public class StartState extends OTAState {

	public static final String name = "Start";

	StartState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		device.sendPacket(BPOTAPacketBuilder.builder.createStartPacket());
	}

	@Override
	protected void onExit() {
	}

	@Override
	protected void onBPOTAResponse(byte result, byte[] payload) {
		if (result == RSP_OK) {
			chageState(UpdateState.name);
		} else if (result == RSP_FAIL) {
			log("RSP_FAIL");
			finalState();
		} else {
			log("Invalid RSP Result: " + result);
			finalState();
		}
	}
}
