package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.StateFactory;

public class BPOTAStateFactory implements StateFactory {

	public static final String DefaultState = "";

	private final StateContext stateContext;
	private final HPatch3Device device;

	public BPOTAStateFactory(StateContext stateContext, HPatch3Device device) {
		this.stateContext = stateContext;
		this.device = device;
	}

	public State createState(String name) {
		switch (name) {
			case ReadModeState.name:
				return new ReadModeState(stateContext, device);
			case EraseFlashState.name:
				return new EraseFlashState(stateContext, device);
			case ProgramFWState.name:
				return new ProgramFWState(stateContext, device);
			case ProgramCRCState.name:
				return new ProgramCRCState(stateContext, device);
			case ReadIDState.name:
			default:
				return new ReadIDState(stateContext, device);
		}
	}
}
