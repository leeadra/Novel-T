package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.StateFactory;

public class OTAStateFactory implements StateFactory {

	public static final String DefaultState = "";

	private final StateContext stateContext;
	private final HPatch3Device device;

	public OTAStateFactory(StateContext stateContext, HPatch3Device device) {
		this.stateContext = stateContext;
		this.device = device;
	}

	public State createState(String name) {
		switch (name) {
            case StartState.name:
                return new StartState(stateContext, device);
            case UpdateState.name:
                return new UpdateState(stateContext, device);
            case StopState.name:
                return new StopState(stateContext, device);
			case ReadyState.name:
			default:
				return new ReadyState(stateContext, device);
		}
	}
}
