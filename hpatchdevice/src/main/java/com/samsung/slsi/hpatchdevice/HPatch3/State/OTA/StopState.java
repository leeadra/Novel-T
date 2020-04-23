package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA;

import com.samsung.slsi.hpatchdevice.HPatch3.BPOTAPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateContext;

public class StopState extends OTAState {

	public static final String name = "Stop";

	StopState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		device.sendPacket(BPOTAPacketBuilder.builder.createStopPacket());
	}

	@Override
	protected void onExit() {
	}

	@Override
	protected void onBPOTAResponse(byte result, byte[] payload) {
		if (result == RSP_OK) {
			log("RSP_OK");
		} else if (result == RSP_FAIL) {
			log("RSP_FAIL");
		} else {
			log("Invalid RSP Result: " + result);
		}
		finalState();
	}
}
