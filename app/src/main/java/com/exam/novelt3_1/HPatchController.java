package com.exam.novelt3_1;

import android.os.Environment;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.samsung.slsi.HPatch;
import com.samsung.slsi.HPatch3DataManager;
import com.samsung.slsi.HPatchBLEException;
import com.samsung.slsi.HPatchConnectionObserver;
import com.samsung.slsi.HPatchDataManager;
import com.samsung.slsi.HPatchDetectObserver;
import com.samsung.slsi.HPatchDeviceBLEInfo;
import com.samsung.slsi.HPatchHealthDataObserver;
import com.samsung.slsi.HPatchManager;
import com.samsung.slsi.hpatchalgorithm.HPatchAlgorithmManager;

import java.io.IOException;

public class HPatchController implements HPatchDetectObserver, HPatchConnectionObserver {
    Application app;

    HPatchManager hPatchManager;
    HPatch hPatch;
    HPatch3DataManager dataManager;
    HPatchAlgorithmManager algorithmManager;
    HPatchHealthDataObserver healthDataObserver;

    String targetAddress;

    Animation anim;

    public HPatchController(HPatch hpatch, HPatchHealthDataObserver observer) throws IOException, HPatchBLEException {
        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(800);
        anim.setStartOffset(0);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        app = Application.getInstance();
        hPatchManager = app.gethPatchManager();
        healthDataObserver = observer;

        setup(hpatch);
    }

    public void setup(HPatch patch) throws IOException, HPatchBLEException {
        hPatch = patch;
        HPatchDeviceBLEInfo info = hPatch.getBLEInfo();
        targetAddress = info.address;
        dataManager = new HPatch3DataManager(app.getHostOS(), hPatch, Environment.getExternalStorageDirectory() + "/NovelT/");
        dataManager.addObserver(healthDataObserver);
        hPatch.addECGObserver(dataManager);
        hPatch.addStatusObserver(dataManager);
        hPatch.addBeatDetectObserver(dataManager);
        hPatch.addHeartRateObserver(dataManager);
        algorithmManager = new HPatchAlgorithmManager(hPatch, dataManager.getBeatDetect());
        dataManager.setAlgorithmController(algorithmManager);
        algorithmManager.addObserver(dataManager);
        hPatch.addConnectionObserver(this);
        hPatch.connect();
    }

    public void clear() {
        hPatchManager.removeHPatchDeviceObserver(this);
        hPatch.disconnect();
        hPatch.removeConnectionObserver(this);
        hPatch.removeECGObserver(dataManager);
        hPatch.removeStatusObserver(dataManager);
        hPatch.removeBeatDetectObserver(dataManager);
        hPatch.removeHeartRateObserver(dataManager);
        try {
            dataManager.clear();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHPatchDeviceConnected(HPatch hPatch) {

    }

    @Override
    public void onHPatchDeviceDisconnected(HPatch hPatch) {
        clear();

        /*try {
            hPatchManager.addHPatchDeviceObserver(this);
            hPatchManager.startScanning();
        } catch (HPatchException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onHPatchDetected(HPatch hPatch) {
        if(hPatch != null) {
            if(hPatch.getBLEInfo().address.compareTo(targetAddress) == 0) {
                hPatchManager.stopScanning();
                hPatchManager.removeHPatchDeviceObserver(this);
                try {
                    setup(hPatch);
                    //activity.setupGUI(hTreadmill, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public HPatchDataManager getDataManager() {
        return dataManager;
    }
}
