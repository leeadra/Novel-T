package com.exam.novelt3_1;

import android.app.Activity;
import android.os.Environment;

import com.samsung.slsi.HPatch;
import com.samsung.slsi.HPatchHostOS;
import com.samsung.slsi.HPatchManager;
import com.samsung.slsi.hpatchdevice.HPatchDeviceManager;
import com.samsung.slsi.hpatchhost.HPatchHostAndroid;

import java.util.HashMap;

public class Application {
    HPatchHostAndroid hPatchHost;
    HPatchManager hPatchManager;

    static Application app = new Application();
    HashMap<HPatch, HPatchController> hPatchSupporters = new HashMap<>();

    static Application getInstance() {
        return app;
    }

    public void initialize(Activity activity) throws Exception {
        if(hPatchHost == null) {
            hPatchHost = new HPatchHostAndroid(activity, Environment.getExternalStorageDirectory() + "/NovelT/");
            hPatchHost.initialize();
        }
        if(hPatchManager == null) {
            hPatchManager = new HPatchDeviceManager(hPatchHost, hPatchHost);
        }
    }

    public HPatchHostOS getHostOS() {
        return hPatchHost;
    }

    public HPatchManager gethPatchManager() {
        return hPatchManager;
    }

    public HPatchController getSuppoerter(HPatch hPatch) {
        return hPatchSupporters.get(hPatch);
    }

    public void setupHPatch(HPatch hPatch, HPatchController supporter) {
        hPatchSupporters.put(hPatch, supporter);
    }

    public void clearHPatch(HPatch hPatch) {
        HPatchController supporter = hPatchSupporters.get(hPatch);
        if(supporter != null) {
            supporter.clear();
            hPatchSupporters.remove(hPatch);
        }
    }
}
