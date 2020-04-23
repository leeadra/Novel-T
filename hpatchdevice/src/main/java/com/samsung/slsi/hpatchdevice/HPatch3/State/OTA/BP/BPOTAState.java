package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;
import com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.OTAState;
import com.samsung.slsi.hpatchdevice.StateContext;

public abstract class BPOTAState extends OTAState {
	public BPOTAState(String name, StateContext stateContext, HPatch3Device device) {
		super("BP-" + name, stateContext, device);
	}

	@Override
	public void onPacketReceived(HPatch3Packet packet) {
	}

	@Override
	protected void onBPOTAResponse(byte result, byte[] payload) {
	}

	public abstract void onBPBootROMPacket(byte result, byte cmd, byte addr, int data, byte[] payload);
}
