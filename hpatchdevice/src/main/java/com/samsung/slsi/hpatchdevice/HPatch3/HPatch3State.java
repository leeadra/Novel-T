package com.samsung.slsi.hpatchdevice.HPatch3;

import com.samsung.slsi.hpatchdevice.State;

public interface HPatch3State extends State {
	void onPacketReceived(HPatch3Packet packet);
	void onDisconnected();
}
