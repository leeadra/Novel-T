package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP;

import com.samsung.slsi.hpatchdevice.HPatch3.BPBootROMPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.BPOTACRC;
import com.samsung.slsi.hpatchdevice.HPatch3.BPOTAPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateContext;

import java.util.Locale;

public class ProgramCRCState extends BPOTAState {
	public static final String name = "Program CRC";
    private final ProgramSender programSender = new ProgramSender(device);

    ProgramCRCState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		try {
			byte[] fw = device.getBPFWData();
			if (fw == null) {
				throw new Exception("Invalid FW Data: " + device.getBP().getTargetPath());
			}
			byte[] crc = BPOTACRC.crc.create(fw);

            if (crc.length > 256) {
                throw new Exception("Invalid CRC Length: " + crc.length + " bytes");
            }

			final int size = 256;
			byte[] data = new byte[size];
			for (int i = 0; i < crc.length; i++) {
				data[i] = crc[i];
			}

			int programAddress = 8160;
            programSender.send(BPOTAPacketBuilder.builder.createProgramPacket(programAddress, data));
		} catch (Exception e) {
			e.printStackTrace();
			log(e.getLocalizedMessage());
			finalState();
		}
	}

	@Override
	protected void onExit() {
	}

	@Override
	public void onBPBootROMPacket(byte result, byte cmd, byte addr, int data, byte[] payload) {
		if (result == RSP_OK) {
			if (cmd == BPBootROMPacketBuilder.BP_BOOTROM_CMD_READ_RESPONSE && data == 0x0002) {
				finalState();
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

	private void sendProgram(byte[] data) {
        programSender.send(data);
    }
}
