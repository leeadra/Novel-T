package com.samsung.slsi.hpatchdevice.HPatch3;

import com.samsung.slsi.OTA;
import com.samsung.slsi.OTAObserver;
import com.samsung.slsi.HPatchHostOS;

/**
 * Created by ch36.park on 2017. 5. 17..
 */

public class BPOTA implements OTA {
    private final String key = "SPatch3_BP_FW_Path";

    private final HPatch3Device device;
    private final HPatchHostOS hostOS;
    private String path;

    public BPOTA(HPatch3Device device, HPatchHostOS hostOS) {
        this.device = device;
        this.hostOS = hostOS;

        this.path = hostOS.getPreference(key);
    }

    @Override
    public String getTargetPath() {
        return path;
    }

    @Override
    public void setTargetPath(String path) {
        this.path = path;

        hostOS.setPreference(key, path);
    }

    @Override
    public void startUpdate(OTAObserver observer) {
        if (device.isConnected()) {
            device.startBPOTA(observer);
        }
    }
}
