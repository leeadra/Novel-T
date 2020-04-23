package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Packet;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketType;
import com.samsung.slsi.hpatchdevice.HPatch3.State.HPatch3StateBase;
import com.samsung.slsi.hpatchdevice.StateContext;

public abstract class OTAState extends HPatch3StateBase {
	protected static final byte RSP_OK = 0x00;
	protected static final byte RSP_FAIL = 0x01;

	public OTAState(String name, StateContext stateContext, HPatch3Device device) {
		super("OTA-" + name, stateContext, device);
	}

	protected abstract void onBPOTAResponse(byte result, byte[] payload);

	@Override
	public void onPacketReceived(HPatch3Packet packet) {
		if (packet.getType() == HPatchPacketType.BP_OTA_RSP) {
			byte[] payload = packet.getPayload();
			byte result = payload[0];
			onBPOTAResponse(result, payload);
		}
	}
}
