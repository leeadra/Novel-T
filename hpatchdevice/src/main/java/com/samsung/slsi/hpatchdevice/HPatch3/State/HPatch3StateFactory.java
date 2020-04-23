package com.samsung.slsi.hpatchdevice.HPatch3.State;

import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateFactory;

public class HPatch3StateFactory implements StateFactory {
    public static final String DefaultState = "";

	private final StateContext stateContext;
	private final HPatch3Device device;

	public HPatch3StateFactory(StateContext stateContext, HPatch3Device device) {
		this.stateContext = stateContext;
		this.device = device;
	}

	public State createState(String name) {
		switch (name) {
			case DeviceInformationState.name:
				return new DeviceInformationState(stateContext, device);
			case ModeSettingState.name:
				return new ModeSettingState(stateContext, device);
			case RegistrationState.name:
				return new RegistrationState(stateContext, device);
			case StartState.name:
				return new StartState(stateContext, device);
			case WorkingState.name:
				return new WorkingState(stateContext, device);
			case FWUpdateState.name:
				return new FWUpdateState(stateContext, device);
			case FinalState.name:
				return new FinalState(stateContext, device);
            case KeepAliveCheckState.name:
            default:
                return new KeepAliveCheckState(stateContext, device);
		}
	}
}
