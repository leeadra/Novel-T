package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP;

import com.samsung.slsi.hpatchdevice.HPatch3.BPBootROMPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.BPOTAPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateContext;

import java.util.Locale;

public class ReadModeState extends BPOTAState {
	public static final String name = "Read Mode";

	ReadModeState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		device.sendPacket(BPOTAPacketBuilder.builder.createReadModePacket());
	}

	@Override
	protected void onExit() {

	}

	@Override
	public void onBPBootROMPacket(byte result, byte cmd, byte addr, int data, byte[] payload) {
		if (result == RSP_OK) {
			if (cmd == BPBootROMPacketBuilder.BP_BOOTROM_CMD_READ_RESPONSE) {
				log(String.format(Locale.getDefault(), "DEVICE MODE: %04X", data));

				chageState(EraseFlashState.name);
			} else {
				log(String.format(Locale.getDefault(), "CMD(%02X) ADDR(%02X) DATA(%04X)", cmd, addr, data));
			}
		} else if (result == RSP_FAIL) {
			log("RSP_FAIL");
			finalState();
		} else {
			log("Invalid RSP Result: " + result);
			finalState();
		}
	}

}
