package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP;

import com.samsung.slsi.hpatchdevice.HPatch3.BPBootROMPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.BPOTAPacketBuilder;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.StateContext;

import java.util.Locale;

public class ProgramFWState extends BPOTAState {
	public static final String name = "Program FW";
	private byte[] fw;
	private int index;
	private int programAddress;

    private byte[] sendingBlock;
    private int validTransferSize;

    private final ProgramSender programSender = new ProgramSender(device);

	ProgramFWState(StateContext stateContext, HPatch3Device device) {
		super(name, stateContext, device);
	}

	@Override
	protected void onEnter() {
		fw = device.getBPFWData();
		log("FW Size: " + fw.length + " bytes");

		index = 0;
		programAddress = 32;

        sendingBlock = BPOTAPacketBuilder.builder.createProgramPacket(programAddress, getProgramData());
        send();
    }

    private void send() {
        programSender.send(sendingBlock);
        setTimeout(timeoutRunnable, 2000);
    }

    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            log("Unstable transfer - sending again");
            programSender.send(sendingBlock);
        }
    };

    private byte[] getProgramData() {
		final int size = 256;
		byte[] data = new byte[size];
		int targetSize = getRemainedProgramData() >= size ? size : getRemainedProgramData();
		System.arraycopy(fw, index, data, 0, targetSize);
        validTransferSize = targetSize;
		return data;
	}

	private int getRemainedProgramData() {
		return fw.length - index;
	}

	@Override
	protected void onExit() {
        cancelTimeout();
	}

	@Override
	public void onBPBootROMPacket(byte result, byte cmd, byte addr, int data, byte[] payload) {
		if (result == RSP_OK) {
			if (cmd == BPBootROMPacketBuilder.BP_BOOTROM_CMD_READ_RESPONSE) {
				programAddress++;
				index += validTransferSize;

				log(String.format(Locale.getDefault(), "Program FW %.1f percent - %d/%d Bytes", getProgressPercentage(), index, fw.length));
				if (getRemainedProgramData() > 0) {
                    cancelTimeout();
                    sendingBlock = BPOTAPacketBuilder.builder.createProgramPacket(programAddress, getProgramData());
                    send();
				} else {
					log("Complete Download");
					chageState(ProgramCRCState.name);
				}
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

	private float getProgressPercentage() {
		return 100.f * index / fw.length;
	}

}
