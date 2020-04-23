package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP;

import com.samsung.slsi.hpatchdevice.HPatch3.BPBootROMPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.BPOTAPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateContext;

import java.util.Locale;

public class EraseFlashState extends BPOTAState {
	public static final String name = "Erase Flash";

	EraseFlashState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		log("Erase Flash: It would take about 10 seconds more.");
		device.sendPacket(BPOTAPacketBuilder.builder.createEraseFlashPacket());
	}

	@Override
	protected void onExit() {

	}

	@Override
	public void onBPBootROMPacket(byte result, byte cmd, byte addr, int data, byte[] payload) {
		if (result == RSP_OK) {
			if (cmd == BPBootROMPacketBuilder.BP_BOOTROM_CMD_READ_RESPONSE) {
				log(String.format(Locale.getDefault(), "Response: %04X", data));

				chageState(ProgramFWState.name);
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
