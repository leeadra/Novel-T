package com.samsung.slsi.hpatchdevice;

import com.samsung.slsi.HPatchDeviceBLEInfo;
import com.samsung.slsi.HPatchHostBLE;
import com.samsung.slsi.HPatchHostOS;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;

/**
 * Created by ch36.park on 2017. 6. 13..
 */

class HPatchDeviceFactory {
    static HPatchDevice getDevice(HPatchDeviceBLEInfo hPatchDeviceBLEInfo, HPatchHostBLE hPatchHostBLE, HPatchHostOS hostOS) {
        HPatchDevice device = null;

        if (HPatch3Device.isHPatch3Device(hPatchDeviceBLEInfo)) {
            device = new HPatch3Device(hPatchDeviceBLEInfo, hPatchHostBLE, hostOS);
        }

        return device;
    }
}
