/*
 * Sensor Product Development Team, System LSI division.
 * Copyright (c) 2014-2017 Samsung Electronics, Inc.
 * All right reserved.
 *
 * This software is the confidential and proprietary information
 * of Samsung Electronics, Inc. (Confidential Information). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Samsung Electronics.
*/
/**
 *******************************************************************************
 * @file		HPatch3Device.java
 * @brief		SPatch3 Device
 *
 * @author		Sensor Product Development Team
 * @author		Chung-Hwan Park (ch36.park@samsung.com)
 * @version		0.2
 * @date		2016/11/29
 *
 * <b>revision history :</b>
 * - 2016/11/29 First creation
 * - 2017/05/19 OTA Support
 *******************************************************************************
 */

package com.samsung.slsi.hpatchdevice.HPatch3;

import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.samsung.slsi.AccelerometerInformation;
import com.samsung.slsi.AccelerometerSensor;
import com.samsung.slsi.FallDetectInformation;
import com.samsung.slsi.FallDetectObserver;
import com.samsung.slsi.FallDetectSensor;
import com.samsung.slsi.FallType;
import com.samsung.slsi.FileLogUtil;
import com.samsung.slsi.GyroscopeInformation;
import com.samsung.slsi.GyroscopeSensor;
import com.samsung.slsi.HPatchBLEException;
import com.samsung.slsi.HPatchHostOS;
import com.samsung.slsi.HPatchSimpleValueContainer;
import com.samsung.slsi.HPatchValue;
import com.samsung.slsi.HPatchValueContainer;
import com.samsung.slsi.OTA;
import com.samsung.slsi.OTAObserver;
import com.samsung.slsi.PostureDetectInformation;
import com.samsung.slsi.PostureDetectObserver;
import com.samsung.slsi.PostureDetectSensor;
import com.samsung.slsi.PostureType;
import com.samsung.slsi.HPatchBLEHeartRateObserver;
import com.samsung.slsi.HPatchDeviceBLEInfo;
import com.samsung.slsi.HPatchError;
import com.samsung.slsi.HPatchHostBLE;
import com.samsung.slsi.HPatchMemoryAccessA8;
import com.samsung.slsi.HPatchOTA;
import com.samsung.slsi.HPatchOperationManager;
import com.samsung.slsi.HPatchTest;
import com.samsung.slsi.HPatchTestObserver;
import com.samsung.slsi.SkinTemperatureInformation;
import com.samsung.slsi.SkinTemperatureSensor;
import com.samsung.slsi.TimeUtils;
import com.samsung.slsi.hpatchdevice.HPatchUtil;
import com.samsung.slsi.hpatchalgorithm.LeadOnOff;
import com.samsung.slsi.hpatchdevice.BatteryCR2032;
import com.samsung.slsi.hpatchdevice.CRC16;
import com.samsung.slsi.hpatchdevice.HPatch3.State.FWUpdateState;
import com.samsung.slsi.hpatchdevice.HPatch3.State.FinalState;
import com.samsung.slsi.hpatchdevice.HPatch3.State.HPatch3StateFactory;
import com.samsung.slsi.hpatchdevice.HPatch3.Mu.MuAccelerometerInformation;
import com.samsung.slsi.hpatchdevice.HPatch3.Mu.MuFallDetectInformation;
import com.samsung.slsi.hpatchdevice.HPatch3.Mu.MuGyroscopeInformation;
import com.samsung.slsi.hpatchdevice.HPatch3.Mu.MuPostureDetectInformation;
import com.samsung.slsi.hpatchdevice.HPatchDevice;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.StateFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.CRC32;

import static com.samsung.slsi.FileLogUtil.isInteralRelease;
import static com.samsung.slsi.FileLogUtil.isStoreEnabled;

import static com.samsung.slsi.FileLogUtil.log;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device.AdditionalFeature.IMU;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device.AdditionalFeature.AccelerometerSignal;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device.AdditionalFeature.GyroscopeSignal;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device.AdditionalFeature.SkinTemperature;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device.AdditionalFeature.SkinTemperatureSignal;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketSource.Accelerometer;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketSource.ECG;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketSource.Gyroscope;
import static com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketSource.None;


public class HPatch3Device extends HPatchDevice
        implements
        StateContext,
        HPatchTest,
        HPatchBLEHeartRateObserver,
        HPatchOTA,
        HPatchOperationManager,
        HPatchMemoryAccessA8,
        FallDetectSensor,
        PostureDetectSensor,
        AccelerometerSensor,
        GyroscopeSensor,
        SkinTemperatureSensor
{

    private static final String TAG = HPatch3Device.class.getSimpleName();

    private static final float DEVICE_ECG_SAMPLES_PER_SECOND = 512 * 1000 / 500 / 4;
    private static final float SAMPLES_PER_SECOND = 512 * 1000 / 500 / 4;
    private static final int SAMPLES_PER_PACKET = 512 / 4;
    private static final float PACKETS_PER_SECOND = SAMPLES_PER_SECOND / SAMPLES_PER_PACKET;

    //  512sample / 547ms - measured
    //** 512 sample / 512 ms - original intent

    public static final int ADC_RANGE = 4096;
    //public static final int ADC_GAIN = 100;  //2016/05/16 Confirmed by Eunjae Hyun
    public static final int ADC_GAIN = 94;  //2016/05/19 Only for FDA TEST
    public static final int INPUT_MILLIVOLTAGE_RANGE = 2200;

    //public static final float UNIT_MILLI_VOLTAGE = 350f;
    public static final float UNIT_MILLI_VOLTAGE = ADC_RANGE * ADC_GAIN / INPUT_MILLIVOLTAGE_RANGE;

    private static final int RETRY_DELAY_MILLIS = 5000;

    final byte ECGOn = (byte) 0x01;
    final byte AESOn = (byte) 0x02;
    final byte BeatDetectionOn = (byte) 0x80;

    private HPatchHostOS hostOS;
    private int bleFWVersion;

    private HPatch3PacketParser packetParser = new HPatch3PacketParser();

    private LeadOnOff leadOnOff;
    private Boolean isLeadOn = null;

    private int lastSequence;
    private int lastAccelerometerSequence;
    private int lastGyroscopeSequence;

    private HPatch3Cipher cipher = null;

    private StateFactory stateFactory = new HPatch3StateFactory(this, this);
    private HPatch3State state = null;

    private boolean isPrime;
    private boolean isOnePrime;

    enum TransferMode {
        PeriodicTransfer(0),
        ResponseTransfer(1),

        ;

        ///////////////////////////////
        // Methods for value setting

        private final int value;
        TransferMode(int value) {
            this.value = value;
        }
        public int getValue() { return value; }

        private static Map<Integer, TransferMode> map = new HashMap<>();
        static {
            for (TransferMode type : TransferMode.values()) {
                map.put(type.value, type);
            }
        }
        public static TransferMode valueOf(int type) {
            return map.get(type);
        }
    }
    private TransferMode transferMode = TransferMode.PeriodicTransfer;


    enum AdditionalFeature {
        IMU(0x01),
        AccelerometerSignal(0x02),
        GyroscopeSignal(0x04),

        SkinTemperature(0x10),
        SkinTemperatureSignal(0x20),

        ;

        ///////////////////////////////
        // Methods for value setting

        private final int value;
        AdditionalFeature(int value) {
            this.value = value;
        }
        public int getValue() { return value; }

        private static Map<Integer, AdditionalFeature> map = new HashMap<>();
        static {
            for (AdditionalFeature type : AdditionalFeature.values()) {
                map.put(type.value, type);
            }
        }
        public static AdditionalFeature valueOf(int type) {
            return map.get(type);
        }
    }

    boolean isIMUEnabled;
    boolean isAccelerometerSignalEnabled;
    boolean isGyroscopeSignalEnabled;

    boolean isSkinTemperatureEnabled;
    boolean isSkinTemperatureSignalEnabled;

    public static boolean isHPatch3Device(HPatchDeviceBLEInfo deviceBLEInfo) {
        if (deviceBLEInfo == null) {
            return false;
        } else {
            if (deviceBLEInfo.name == null) {
                return false;
            } else {
                if(deviceBLEInfo.name.contains("Novel-T")) {
                    return true;
                }
                if (!deviceBLEInfo.name.equals("S-PATCH3")) {
                    return false;
                } else {
                    int major = HPatchUtil.getMajor(deviceBLEInfo.scanRecord);
                    return !(major != 0x00005348 && major != 20545);
                }
            }
        }
    }

    public HPatch3Device(HPatchDeviceBLEInfo deviceBLEInfo,
                         HPatchHostBLE hPatchHostBLE,
                         HPatchHostOS hostOS) {
        super(deviceBLEInfo, hPatchHostBLE, UNIT_MILLI_VOLTAGE, DEVICE_ECG_SAMPLES_PER_SECOND, SAMPLES_PER_SECOND, SAMPLES_PER_PACKET, new LeadOnOff());

        this.hostOS = hostOS;
        leadOnOff = (LeadOnOff)getLeadDetect();

        bleFWVersion = HPatchUtil.getBLEFWVersion(deviceBLEInfo.scanRecord);

        lastSequence = 0;
    }

    @Override
    protected void initialize() throws HPatchBLEException {
        lastSequence = 0;
        lastAccelerometerSequence = 0;
        lastGyroscopeSequence = 0;

        hPatchHostBLE.addBLEHeartRateObserver(deviceBLEInfo.address, this);

        changeState(HPatch3StateFactory.DefaultState);
    }

    @Override
    protected void clear() {
        //log("Clear HPatch3Device: " + this.getId());
        if (state != null) {
            state.onDisconnected();
        }

        hPatchHostBLE.removeBLEHeartRateObserver(deviceBLEInfo.address, this);

        try {
            sendECGStop();
        } catch (HPatchBLEException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(byte[] packet) {
        try {
            if (FileLogUtil.isInteralRelease) {
                String txt = "SendPacket[" + packet.length + "]: ";
                for (byte b : packet) {
                    txt += String.format(Locale.getDefault(), "%02X", b);
                }
                //logOTA(txt);
            }
            write(packet);
        } catch (HPatchBLEException e) {
            //log(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private byte ecgOperationMode = 0x01;
    protected void setECGOperationMode(byte mode) {
        ecgOperationMode = mode;
    }

    private void sendECGStart() {
        if (isOnePrime) {
            byte transferModeValue;
            if (nextTransferModeValue >= 0) {
                transferModeValue = (byte) nextTransferModeValue;
                switch (transferModeValue) {
                    case 0:
                        transferMode = TransferMode.PeriodicTransfer;
                        break;
                    case 1:
                    default:
                        transferMode = TransferMode.ResponseTransfer;
                        break;
                }
            } else {
                switch (transferMode) {
                    case PeriodicTransfer:
                        transferModeValue = 0;
                        break;
                    case ResponseTransfer:
                    default:
                        transferModeValue = 1;
                        break;
                }
            }

            //log("TransferMode: " + transferMode + " (" + transferModeValue + ")");
            writeA8(TransferModeAddress, new byte[]{transferModeValue});
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                //log(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        byte mode = (byte)getOperationMode();
        if (mode == 0x00) {
            mode = ecgOperationMode;
        }
        isAESEnabled = ((mode & AESOn) > 0);

        //log("ECG Start: 0x" + String.format(Locale.getDefault(), "%02X", mode));
        sendPacket(HPatch3PacketBuilder.builder.createECGStart(mode));

        firstTime = 0;
        skipCount = 0;

        if (isInteralRelease) {
            HPatchValueContainer container = new HPatchSimpleValueContainer("Send ECG Start");
            {
                HPatchValue value = container.setValue("Operation Mode");
                value.setValue(String.format(Locale.getDefault(), "%02X", mode));
            }
            broadcastSPatchTest(4, container);
        }
    }

    private void sendECGStop() throws HPatchBLEException {
        //log("ECG Stop");

        byte[] ecgStopPacket = HPatch3PacketBuilder.builder.createECGStop();

        for (int i = 0; i < 2; i++) {
            sendPacket(ecgStopPacket);

            if (isInteralRelease) {
                HPatchValueContainer container = new HPatchSimpleValueContainer("Send ECG Stop");
                broadcastSPatchTest(4, container);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        handler.removeCallbacks(requestECGSignalDataRunnable);
        if (isOnePrime) {
            synchronized (handler) {
                handler.removeCallbacks(requestOneSignalRunnable);
                handler.removeCallbacks(requestOneRequestTimeoutRunnable);
            }
        }
    }

    private long firstTime;
    private long prevTime;
    private long totalCount;

    private BatteryCR2032 batteryCR2032 = new BatteryCR2032();

    @Override
    public void onHeartRateMeasurement(HPatchDeviceBLEInfo hPatchDeviceBLEInfo, int heartRate) {
        //log("SPatch3 HR Profile: " + heartRate);
        broadcastHeartRateReceived(heartRate);
    }

    @Override
    public State getCurrentState() {
        return state;
    }

    @Override
    public void changeState(String name) {
        try {
            if (state != null) {
                if (state.getName().equals(FinalState.name)) {
                    //log("Already FinalState");
                    return;
                }

                state.exit();
            }

            State s = stateFactory.createState(name);
            if (s instanceof HPatch3State) {
                state = (HPatch3State) s;
                state.enter();
            } else {
                //log("Invalid State: " + name);
                state = null;
                clear();
            }
        } catch (Exception e) {
            //log(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void finalState() {
        if (state != null) {
            if (state instanceof FinalState) {
                return;
            }
        }
        changeState(FinalState.name);
    }

    private boolean isAESEnabled;
    private boolean isLeadDetectEnabled;

    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String filename = sdf.format(date) + ".txt";

    @Override
    protected void onReadBLE(byte[] packetData) {
        packetParser.add(packetData);
        HPatch3Packet packet;
        while ((packet = packetParser.get()) != null) {
            //debugOutPacketTypeText(packet);

            if (packet.getType() == HPatchPacketType.KeepAliveResponse) {
                broadcastSPatchTest(1, null);
            } else if (packet.getType() == HPatchPacketType.ReadResponse) {
                parseReadResponse(packet);
            }

            state.onPacketReceived(packet);
        }
    }

    private void debugOutPacketTypeText(HPatch3Packet packet) {
        if (FileLogUtil.isInteralRelease && FileLogUtil.isStoreEnabled)  //Todo: remove - Only for debug
        {
            /*log("PacketType: " + packet.getType()
                    + "[" + String.format("%02X", packet.getType().getValue()) + "]"
                    + ", Len: " + packet.getLength()
            );*/
        }
    }

    private void debugOutPacketText(byte[] packetData) {
        String packetText = FileLogUtil.getDateText(System.currentTimeMillis()) + " :: ";
        for (byte b : packetData) {
            packetText += String.format(Locale.getDefault(), "%02X ", b);
        }
        //log(packetText);
    }

    public void parseDeviceInformation(HPatch3Packet packet) {
        int i = 0;
        byte[] payload = packet.getPayload();

        int addressLength = payload[i] & 0xFF;
        i++;
        if (addressLength != 1) {
            //log("" + state + ": " + packet.getType() + ": " + "Invalid Address Length: " + addressLength);
            return;
        }

        int address = payload[i] & 0xFF;
        i++;
        if (address != 0x00) {
            //log("" + state + ": " + packet.getType() + ": " + "Invalid Address: " + String.format("%02X", address));
            return;
        }

        int dataSize = (payload[i] & 0x00FF)
                | ((payload[i+1] << 8) & 0xFF00);
        i += 2;
        if (dataSize != 0x0A) {
            //log("" + state + ": " + packet.getType() + ": " + "Invalid Data Size: " + dataSize);
            return;
        }

        int vendorID = (payload[i] & 0x00FF)
                | ((payload[i + 1] << 8) & 0xFF00);
        i += 2;

        int productID = (payload[i] & 0x00FF)
                | ((payload[i + 1] << 8) & 0xFF00);
        i += 2;

        int chipID = (payload[i] & 0x00FF)
                | ((payload[i + 1] << 8) & 0xFF00);
        i += 2;

        int firmwareVersion = (payload[i] & 0x000000FF)
                | ((payload[i + 1] << 8) & 0x0000FF00)
                | ((payload[i + 2] << 16) & 0x00FF0000)
                | ((payload[i + 3] << 24) & 0xFF000000);
        i += 4;

        {
            String txt = "Device Information:\n";
            txt += String.format("Vendor  ID: %04X\n", vendorID);
            txt += String.format("Product ID: %04X\n", productID);
            txt += String.format("Chip    ID: %04X\n", chipID);
            txt += String.format("FirmwareID: %08X\n", firmwareVersion);

            //log(txt);
        }

        setOperationMode(productID, firmwareVersion);

        try {
            HPatchValueContainer container = new HPatchSimpleValueContainer("DeviceInformation");

            container.setValue("Vendor ID").setValue(String.format("%04X", vendorID));
            container.setValue("Product ID").setValue(String.format("%04X", productID));
            container.setValue("Chip ID").setValue(String.format("%04X", chipID));
            container.setValue("Firmware ID").setValue(String.format("%08X", firmwareVersion));
            container.setValue("BLE FW").setValue(String.format("%06X", bleFWVersion));

            broadcastSPatchTest(3, container);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final int TransferModeAddress = 0x0A;
    static final int AdditionalFeatureAddress = 0x0B;

    int nextTransferModeValue = -1;

    protected void setOperationMode(int productID, int firmwareVersion) {

        isPrime = false;
        isOnePrime = false;

        transferMode = TransferMode.PeriodicTransfer;

        isIMUEnabled = false;
        isAccelerometerSignalEnabled = false;
        isGyroscopeSignalEnabled = false;

        isSkinTemperatureEnabled = false;
        isSkinTemperatureSignalEnabled = false;

        //Refer to AppRelease/__Release_for_internal/15_SPatch3_Firmware/01. SPatch3_BP_Firmware/BioSensor_FW_ReleaseNote.xlsx
        switch (productID) {
            case 0x9339: {   //S-PATCH3 Prime Test (Based on S-PATCH3)
                isPrime = true;
                isOnePrime = false;

                isAESEnabled = true;
                isLeadDetectEnabled = true;

                transferMode = TransferMode.ResponseTransfer;

                setECGOperationMode((byte)(ECGOn | AESOn | BeatDetectionOn));
                break;
            }
            case 0x9331:    //S-PATCH3 Prime
            case 0x9321:    //S-PATCH3 Mu
            {
                isPrime = false;
                isOnePrime = true;

                isAESEnabled = true;
                isLeadDetectEnabled = true;

                isNeedToSetMode = true;

                setECGOperationMode((byte)(ECGOn | AESOn | BeatDetectionOn));
                break;
            }
            case 0x9301:    //S-PATCH3
            default:
            {
                if (firmwareVersion >= 0x00000600) {
                    switch (firmwareVersion) {
                        case 0x00000601: {
                            //SSIC Test FW
                            final int samplesPerPacket = 512 / 4;
                            final int packetsPerSecond = 2;

                            setDeviceECGSamplesPerSecond(samplesPerPacket * packetsPerSecond);
                            setECGTransferSamplesPerSecond(samplesPerPacket * packetsPerSecond);
                            setECGTransferSamplesPerPacket(samplesPerPacket);

                            isAESEnabled = false;
                            isLeadDetectEnabled = false;

                            setECGOperationMode(ECGOn);

                            isPrime = false;
                            isOnePrime = false;
                            break;
                        }
                        case 0x00000600:
                        default: {
                            //Normal Version
                            isAESEnabled = true;
                            isLeadDetectEnabled = true;

                            isNeedToSetMode = false;

                            setECGOperationMode((byte)(ECGOn | AESOn | BeatDetectionOn));

                            isPrime = false;
                            isOnePrime = false;
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    private boolean isNeedToSetMode;
    public boolean isNeedToSetMode() {
        return isNeedToSetMode;
    }

    public void requestModeSetting() {
        isModeSettingDone = false;

        readA8(TransferModeAddress, 2);
    }

    private boolean isModeSettingDone;

    public boolean parseModeSetting(HPatch3Packet packet) {
        if (packet.getType() == HPatchPacketType.ReadResponse) {
            byte[] payload = packet.getPayload();
            int addressLength = payload[0];
            switch (addressLength) {
                case 1: {
                    int address = payload[1];
                    int length = (payload[2] + ((payload[3] & 0xFF) << 8));
                    if (4 + length > payload.length) {
                        //log("Invalid Length: " + length + " in Packet[" + payload.length + "]");
                    } else {
                        byte[] data = Arrays.copyOfRange(payload, 4, 4 + length);

                        parseSPatchMemoryReadA8(address, length, data);
                    }
                    break;
                }
            }
        }
        return isModeSettingDone;
    }

    public void parseSPatchMemoryReadA8(int address, int length, byte[] data) {
        if (data != null && length == data.length) {
            switch (address) {
                case TransferModeAddress: {
                    if (data.length == 2) {
                        transferMode = TransferMode.valueOf(data[0]);

                        int additionalFeatureSetting = data[1];

                        isIMUEnabled = ((additionalFeatureSetting & IMU.getValue()) > 0);
                        isAccelerometerSignalEnabled = ((additionalFeatureSetting & AccelerometerSignal.getValue()) > 0);
                        isGyroscopeSignalEnabled = ((additionalFeatureSetting & GyroscopeSignal.getValue()) > 0);

                        isSkinTemperatureEnabled = ((additionalFeatureSetting & SkinTemperature.getValue()) > 0);
                        isSkinTemperatureSignalEnabled = ((additionalFeatureSetting & SkinTemperatureSignal.getValue()) > 0);

                        isModeSettingDone = true;
                    }
                    break;
                }
            }
        }
    }

    public void setEncryptedTransferKey(byte[] encryptedTransferKey) throws Exception {
        HPatch3Cipher cipher = new HPatch3Cipher(HPatch3HostKey.getHostKey(hostOS));
        byte[] transferKey = cipher.decrypt(encryptedTransferKey);

        if (FileLogUtil.isInteralRelease && FileLogUtil.isStoreEnabled) {   //ToDo: Remove this - Only for Test
            HPatchValueContainer container = new HPatchSimpleValueContainer("ResponseTransferKey");
            {
                HPatchValue value = container.setValue("EncryptedKey");
                String txt = "";
                for (int i = 0; i < encryptedTransferKey.length; i++) {
                    txt += String.format("%02X", encryptedTransferKey[i]);
                }
                value.setValue(txt);
                //log("EncryptedKey: " + txt);
            }
            {
                HPatchValue value = container.setValue("TransferKey");
                String txt = "";
                for (int i = 0; i < transferKey.length; i++) {
                    txt += String.format("%02X", transferKey[i]);
                }
                value.setValue(txt);
                //log("TransferKey: " + txt);
            }

            broadcastSPatchTest(2, container);
        }

        //boolean isValidTransferKey = false;
        boolean isValidTransferKey = true;
        for (byte b : transferKey) {
            if (b != (byte) 0xff) {
                isValidTransferKey = true;
                break;
            }
        }

        if (!isValidTransferKey) {
            final String errorMessage = "Registration is denied";
            broadcastError(HPatchError.RegistrationDenied, errorMessage);
            throw new Exception(errorMessage);
        } else {
            readyCipher(transferKey);
        }
    }

    public void requestECGStart() {
        //log("Request ECG Start: " + transferMode);

        sendECGStart();

        if (TransferMode.ResponseTransfer == transferMode) {
            if (isPrime) {
                handler.postDelayed(requestECGSignalDataRunnable, 2000);
            } else if (isOnePrime) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                requestSignalTransfer();
            }
        }
    }

    private void readyCipher(byte[] transferKey) {
        if (isAESEnabled) {
            try {
                cipher = new HPatch3Cipher(transferKey); //, SampleInitialVector);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            cipher = null;
        }
    }

    private int invalidSequenceCount;
    private int invalidCRCCount;

    private int skipCount;

    private final Handler handler = new Handler();

    public boolean parsePacket(HPatch3Packet packet) {
        if (packet.getType() == HPatchPacketType.Battery) {
            parseBatteryPacket(packet);
            return true;
        } else {
            List<HPatch3Packet> compositePackets = packet.getSubPacket(HPatchPacketType.Composite);
            if (compositePackets == null) {
                if (packet.getType() == HPatchPacketType.Composite) {
                    compositePackets = new ArrayList<>();
                    compositePackets.add(packet);
                }
            }
            if (compositePackets != null) {
                for (HPatch3Packet compositePacket : compositePackets) {
                    if (isAccelerometerPacket(compositePacket)) {
                        broadcastAccelerometerUpdated(getAccelerometerInformation(packet));
                    }
                    if (isGyroscopePacket(compositePacket)) {
                        broadcastGyroscopeUpdated(getGyroscopeInformation(packet));
                    }
                }
            }

            /*if (hasFallDetectPacket(packet)) {
                FallDetectInformation information = getFallDetectInformation(packet);
                broadcastFallDetect(information);
            }

            if (hasPostureDetectPacket(packet)) {
                PostureDetectInformation information = getPostureDetectInformation(packet);
                broadcastPostureDetect(information);
            }*/

            if (isECGPacket(packet)) {
                return parseECGPacket(packet);
            } else {
                return false;
            }
        }
    }

    private boolean parseReadResponse(HPatch3Packet packet) {
        byte[] payload = packet.getPayload();
        int addressLength = payload[0];
        switch (addressLength) {
            case 1: {
                int address = payload[1];
                int length = (payload[2] + ((payload[3] & 0xFF) << 8));
                if (4 + length > payload.length) {
                    //log("Invalid Length: " + length + " in Packet[" + payload.length + "]");
                } else {
                    byte[] data = Arrays.copyOfRange(payload, 4, 4 + length);

                    synchronized (memoryAccessA8ReadListeners) {
                        for (ReadListener readListener : memoryAccessA8ReadListeners) {
                            readListener.onSPatchMemoryReadA8(address, length, data);
                        }
                    }
                }
                break;
            }
        }
        return false;
    }

    public boolean parseECGPacket(HPatch3Packet packet) {
        final int lostPacketLimit = 120; // 2 packets per second * 60 sec = 120
        boolean isValid = true;

        int sequenceNumber = getSequenceNumber(packet);

        if (TransferMode.ResponseTransfer == transferMode) {
            if (isPrime) {
                synchronized (handler) {
                    handler.removeCallbacks(requestECGSignalDataRunnable);
                }
            }
            if (isOnePrime) {
                synchronized (handler) {
                    handler.removeCallbacks(requestOneRequestTimeoutRunnable);
                }
            }

            if (skipCount > 0) {
                //log("Seq.Num: " + sequenceNumber + " Skip: " + skipCount);
                skipCount--;
                lastSequence = 0;
                isValid = false;
            } else if (sequenceNumber <= lastSequence) {
                //log("Seq.Num: " + sequenceNumber + " Already Received Signal");
                isValid = false;
            } else if (sequenceNumber > lastSequence + 1) {
                //log("Seq.Num: " + sequenceNumber + " Some signals are missing, drop this");
                isValid = false;
            }
        }

        if (isValid) {
            //log("ECG Seq.Num: " + sequenceNumber);

            HPatch3Packet streamPacket = packet.getFirstSubPacket(HPatchPacketType.Stream16Bit);
            if (streamPacket != null) {
                int[] ecgSignal = streamPacket.getPayloadAsInt16Array();

                long time = System.currentTimeMillis();
                if (firstTime == 0) {
                    firstTime = time;
                    prevTime = time;
                }

                if (false && FileLogUtil.isInteralRelease && FileLogUtil.isStoreEnabled) {  // ToDo: Only for debugging
                    //debugOutReceivedECGSignalStatistics(packet, sequenceNumber, ecgSignal, time);
                }

                HPatch3Packet crcPacket = packet.getFirstSubPacket(HPatchPacketType.CRC);
                if (crcPacket != null) {
                    isValid = isValidCRC(streamPacket, crcPacket);
                }

                if (cipher != null && isAESEnabled) {
                    ecgSignal = getDecryptedECGSignal(streamPacket);
                }

                if (false && FileLogUtil.isInteralRelease && FileLogUtil.isStoreEnabled) { //Todo: remove - Only for debug
                    //debugOutECGSignal(ecgSignal);
                }

                if (isValid && isLeadDetectEnabled) {
                    boolean isValidLeadOn = isValidLeadOnState(sequenceNumber, ecgSignal);
                    //isValid = isValidLeadOn;
                }

                if (isValid && TransferMode.PeriodicTransfer == transferMode && lastSequence > 0 && sequenceNumber != lastSequence + 1) {
                    if (Math.abs(sequenceNumber - lastSequence) > lostPacketLimit) {
                        if (false) {
                            // lost more #lostPacketLimit packets
                            final int invalidSequenceCountLimit = 5;

                            invalidSequenceCount++;
                            //log("LastSeq: " + lastSequence + ", Seq: " + sequenceNumber + " (" + invalidSequenceCount + "/" + invalidSequenceCountLimit + ")");
                            //log(errorLogFileName, FileLogUtil.getDateTextForFile(System.currentTimeMillis()) + " LastSeq: " + lastSequence + ", Seq: " + sequenceNumber + " (" + invalidSequenceCount + "/" + invalidSequenceCountLimit + ")");

                            if (invalidSequenceCount > invalidSequenceCountLimit) {
                                disconnect();
                                isValid = false;
                            }
                        } else {
                            // Restart Sequence Number
                            lastSequence = sequenceNumber;
                        }
                    } else {
                        if (isLeadDetectEnabled) {
                            leadOnOff.onSPatchECGPacketLost(this, lastSequence, sequenceNumber);
                        }
                        broadcastECGPacketLost(lastSequence, sequenceNumber);

                        totalCount += SAMPLES_PER_PACKET * (sequenceNumber - lastSequence - 1);
                    }
                }

                if (isValid) {
                    broadcastECGReceived(sequenceNumber, ecgSignal);
                    lastSequence = sequenceNumber;
                    invalidSequenceCount = 0;
                }
            }

            if (isValid) {
                parseECGBeatDetectPacket(packet);
            }

            if (isValid) {
                parseSkinTemperaturePacket(packet);
            }
        }

        if (TransferMode.ResponseTransfer == transferMode) {
            long interval = 500;
            if (isPrime) {
                handler.postDelayed(requestECGSignalDataRunnable, interval);
            } else if (isOnePrime) {
                requestSignalTransfer();
            }
        }

        return true;
    }

    private void parseSkinTemperaturePacket(HPatch3Packet packet) {
        if (false) { //ToDo: Remove this - this only for GUI Testing
            testSkinTemperature += 0.1f;
            broadcastSkinTemperatureUpdated(new HPatch3SkinTemperatureInformation(testSkinTemperature));
        } else {
            HPatch3Packet skinTemperaturePacket = packet.getFirstSubPacket(HPatchPacketType.SkinTemperature);
            if (skinTemperaturePacket != null) {
                byte[] payload = skinTemperaturePacket.getPayload();
                if (payload.length == 4) {
                    float skinTemperature = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    broadcastSkinTemperatureUpdated(new HPatch3SkinTemperatureInformation(skinTemperature));
                }
            }
        }
    }
    private float testSkinTemperature;  //ToDo: Remove this - this only for GUI Testing

    private void parseECGBeatDetectPacket(HPatch3Packet packet) {
        HPatch3Packet beatDetectPacket = packet.getFirstSubPacket(HPatchPacketType.ECGBeatDetect);
        if (beatDetectPacket != null) {
            final int beatDetectDataSize = (4 + 4);
            int count = beatDetectPacket.getLength() / beatDetectDataSize;

            if (count > 0 && count < 10) {
                try {
                    int[] rriIndexData = new int[count];
                    float[] rriValueData = new float[count];

                    byte[] data = beatDetectPacket.getPayload();
                    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                    for (int i = 0; i < count; i++) {
                        rriIndexData[i] = buffer.getInt();
                        rriValueData[i] = buffer.getFloat();
                    }

                    if (FileLogUtil.isInteralRelease && FileLogUtil.isStoreEnabled) {
                        String report = "BeatDetection: \n";
                        for (int i = 0; i < count; i++) {
                            report += String.format(Locale.getDefault(), "Index: %d, Value: %f\n", rriIndexData[i], rriValueData[i]);
                        }
                        //log(report);
                    }

                    broadcastBeatDetectionUpdated(rriIndexData, rriValueData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getSequenceNumber(HPatch3Packet packet) {
        int sequenceNumber = 0;
        HPatch3Packet sequenceNumberPacket = packet.getFirstSubPacket(HPatchPacketType.SequenceNumber);
        if (sequenceNumberPacket != null) {
            sequenceNumber = sequenceNumberPacket.getPayloadAsInt32();
        }
        return sequenceNumber;
    }

    private boolean isValidLeadOnState(int sequenceNumber, int[] ecgSignal) {
        leadOnOff.onUpdateECG(this, sequenceNumber, ecgSignal);
        Boolean isLeadOn = leadOnOff.isLeadOn();
        if (isLeadOn != null) {
            if (this.isLeadOn == null) {
                this.isLeadOn = !isLeadOn;
            }
            if (this.isLeadOn != isLeadOn) {
                this.isLeadOn = isLeadOn;
                log("Lead: " + isLeadOn);

                broadcastLeadStatusUpdated(isLeadOn ? 1 : 0);
            }
        }
        return this.isLeadOn == null ? false : this.isLeadOn;
    }

    private void debugOut(String prefix, byte[] data) {
        String txt = prefix;
        for (byte b : data) {
            txt += String.format("%02X ", b);
        }
        //log(txt);
    }

    private int[] getDecryptedECGSignal(HPatch3Packet streamPacket) {
        int[] ecgSignal = null;

        try {
            if (false) {    //ToDo: only for debugging
                //debugOutCipher(streamPacket);
            }
            if (false) {    //ToDo: only for debugging
                //debugOut("Cipher-Key: ", cipher.key);
                //debugOut("Before: ", streamPacket.getPayload());
            }

            byte[] decryptedBytes = cipher.decrypt(streamPacket.getPayload());

            if (false) {    //ToDo: only for debugging
                //debugOut("After : ", decryptedBytes);
            }

            int len = decryptedBytes.length / 2;
            int[] data = new int[len];
            for (int i = 0; i < len; i++) {
                data[i] = (decryptedBytes[i * 2] & 0x000000ff)
                        + ((decryptedBytes[i * 2 + 1] << 8) & 0x0000ff00);
            }
            ecgSignal = data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ecgSignal;
    }

    private boolean isValidCRC(HPatch3Packet streamPacket, HPatch3Packet crcPacket) {
        boolean isValid = true;
        switch (crcPacket.getLength()) {
            case 2: {
                //CRC16
                int expected = crcPacket.getPayloadAsInt16();
                int actual = CRC16.getCRC(streamPacket.getPayload());

                isValid = (expected == actual);
                if (!isValid) {
                    invalidCRCCount++;
                    //log("ECG: Invalid CRC: " + invalidCRCCount);
                }
                break;
            }
            case 4: {
                //CRC32
                long expected = crcPacket.getPayloadAsInt32();
                CRC32 crc = new CRC32();
                crc.update(streamPacket.getPayload());
                long actual = crc.getValue();

                isValid = (expected == actual);
                if (!isValid) {
                    //log("ECG: Invalid CRC");
                }
                break;
            }
            default: {
                //ToDo: Invalid CRC Handling
                //log("Invalid CRC Handling");
                break;
            }
        }
        return isValid;
    }

    private boolean isECGPacket(HPatch3Packet packet) {
        HPatchPacketSource source = None;
        HPatch3Packet sourcePacket = packet.getFirstSubPacket(HPatchPacketType.Source);
        if (sourcePacket != null) {
            source = HPatchPacketSource.valueOf(sourcePacket.getPayloadAsInt8());
        } else {
            HPatch3Packet channelPacket = packet.getFirstSubPacket(HPatchPacketType.Channel);
            if (channelPacket != null) {
                if (channelPacket.getPayloadAsInt8() == 1) {
                    source = ECG;
                } else {
                    source = ECG;    //assume
                }
            } else {
                //source = ECG;    //assume
            }
        }
        return source == ECG;
    }

    private void debugOutReceivedECGSignalStatistics(HPatch3Packet packet, int sequenceNumber, int[] ecgSignal, long time) {
        int deviceTimeStamp = 0;
        HPatch3Packet keepAliveResponsePacket = packet.getFirstSubPacket(HPatchPacketType.KeepAliveResponse);
        if (keepAliveResponsePacket != null) {
            deviceTimeStamp = keepAliveResponsePacket.getPayloadAsInt32();
        }
        totalCount += ecgSignal.length;
        {
            long elapsedTime = time - firstTime;

            float samplesPerSecond = ((float) totalCount / (elapsedTime / 1000f));
            long expectedSampleCount = (long) ((elapsedTime / 1000) * SAMPLES_PER_SECOND);
            /*log("ECGSignal[" + sequenceNumber + "]: " + ecgSignal.length
                    + ", T: " + deviceTimeStamp
                    + ", Total: " + totalCount
                    + ", Exp: " + expectedSampleCount
                    + ", Sec: " + String.format(Locale.getDefault(), "%.2f", (elapsedTime / 1000f))
                    + ", Elapsed: " + (time - prevTime)
                    + ", Samples Per Second: " + samplesPerSecond
                    + ", Gap: " + (totalCount - expectedSampleCount)
            );*/
            prevTime = time;
        }
    }

    private void debugOutCipher(HPatch3Packet streamPacket) {
        String txt = "Streaming Payload: ";
        byte[] data = streamPacket.getPayload();
        int i = 0;
        for (byte b : data) {
            if ((i % 8) == 0) {
                txt += "\n";
                if ((i % (8 * 8)) == 0) {
                    txt += "\n";
                }
            }
            txt += String.format("(byte) 0x%02X, ", b);
            i++;
        }
        //log(txt);
    }

    private void debugOutECGSignal(int[] ecgSignal) {
        String debugText = "ECG Signal: ";
        for (int n : ecgSignal) {
            debugText += "" + n + ", ";
        }
        //log(debugText);
    }

    private long getElapsedTime(int sequenceNumber) {
        return (long)(sequenceNumber * (1000.0 / PACKETS_PER_SECOND));
    }

    private int getExpectedSequenceNumber() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - firstTime;
        return (int)(elapsedTime / (1000.0 / PACKETS_PER_SECOND));
    }

    private static final int SAVE_TRANSMIT_PACKET_COUNT = 4;

    public void requestECGSignalData() {
        int startSequenceNumber = lastSequence + 1;
        int packetCount = SAVE_TRANSMIT_PACKET_COUNT;

        //log("Seq.Num: " + startSequenceNumber + " [" + packetCount + "] ECG Signals are requested");

        sendPacket(HPatch3PacketBuilder.builder.createRequestECGSignal(startSequenceNumber, packetCount));
    }

    private Runnable requestECGSignalDataRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (handler) {
                handler.removeCallbacks(this);
            }

            requestECGSignalData();
        }
    };

    public void requestOneECGSignalData() {
        int startSequenceNumber = lastSequence + 1;
        int packetCount = SAVE_TRANSMIT_PACKET_COUNT;

        //log("Seq.Num: " + startSequenceNumber + " [" + packetCount + "] ECG Signals are requested");

        sendPacket(HPatch3PacketBuilder.builder.createRequestSignal(startSequenceNumber, packetCount, ECG));
    }

    public void requestOneAccelerometerSignalData() {
        int startSequenceNumber = lastAccelerometerSequence + 1;
        int packetCount = SAVE_TRANSMIT_PACKET_COUNT;

        //log("Seq.Num: " + startSequenceNumber + " [" + packetCount + "] Accelerometer Signals are requested");

        sendPacket(HPatch3PacketBuilder.builder.createRequestSignal(startSequenceNumber, packetCount, Accelerometer));
    }

    public void requestOneGyroscopeSignalData() {
        int startSequenceNumber = lastGyroscopeSequence + 1;
        int packetCount = SAVE_TRANSMIT_PACKET_COUNT;

        //log("Seq.Num: " + startSequenceNumber + " [" + packetCount + "] Gyroscope Signals are requested");

        sendPacket(HPatch3PacketBuilder.builder.createRequestSignal(startSequenceNumber, packetCount, Gyroscope));
    }

    private void requestSignalTransfer() {
        if (TransferMode.ResponseTransfer == transferMode) {
            handler.removeCallbacks(requestOneSignalRunnable);
            handler.postDelayed(requestOneSignalRunnable, 100);
        }
    }

    class TargetSource implements Comparable<TargetSource> {
        int sequenceNumber;
        HPatchPacketSource source;

        TargetSource(int sequenceNumber, HPatchPacketSource source) {
            this.sequenceNumber = sequenceNumber;
            this.source = source;
        }

        @Override
        public int compareTo(@NonNull TargetSource targetSource) {
            return this.sequenceNumber - targetSource.sequenceNumber;
        }
    }

    private Runnable requestOneSignalRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (handler) {
                handler.removeCallbacks(this);
            }

            HPatchPacketSource source;

            List<TargetSource> targets = new ArrayList<>();
            targets.add(new TargetSource(lastSequence, ECG));
            if (isAccelerometerSignalEnabled) {
                targets.add(new TargetSource(lastAccelerometerSequence, Accelerometer));
            }
            if (isGyroscopeSignalEnabled) {
                targets.add(new TargetSource(lastGyroscopeSequence, Gyroscope));
            }

            Collections.sort(targets);
            source = targets.get(0).source;

            switch (source) {
                case Accelerometer: {
                    requestOneAccelerometerSignalData();
                    break;
                }
                case Gyroscope: {
                    requestOneGyroscopeSignalData();
                    break;
                }
                case ECG:
                default: {
                    requestOneECGSignalData();
                    break;
                }
            }

            synchronized (handler) {
                handler.postDelayed(requestOneRequestTimeoutRunnable, 1000);
            }
        }
    };

    private Runnable requestOneRequestTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            requestSignalTransfer();
        }
    };

    private Runnable requestOneECGSignalRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (handler) {
                handler.removeCallbacks(this);
            }

            requestOneECGSignalData();
        }
    };

    public void parseBatteryPacket(HPatch3Packet packet) {
        int batteryStatus = packet.getPayloadAsInt32();
        int batteryRatio = batteryCR2032.getBatteryRatio(batteryStatus);
        if (batteryRatio > 0) {
            broadcastBatteryRatioUpdated(batteryRatio);
        }
    }


    private final ArrayList<HPatchTestObserver> hPatchTestObservers = new ArrayList<>();

    public void broadcastSPatchTest(int id, HPatchValueContainer params) {
        synchronized (hPatchTestObservers) {
            for (HPatchTestObserver observer : hPatchTestObservers) {
                observer.onTestResult(this, id, params);
            }
        }
    }

    @Override
    public void runTest(int id, ArrayList<Object> params) {
        switch (id) {
            case 1: {
                sendKeepAlive();
                break;
            }
            case 2: {
                sendHostKey();
                break;
            }
            case 3: {
                try {
                    sendECGStop();
                } catch (HPatchBLEException e) {
                    e.printStackTrace();
                }
                sendECGStart();
                break;
            }
        }
    }

    public void sendKeepAlive() {
        //log("KeepAlive");
        sendPacket(HPatch3PacketBuilder.builder.createKeepAlive());
    }

    public void requestDeviceInformation() {
        //log("Request Device Information");
        sendPacket(HPatch3PacketBuilder.builder.createRequestDeviceInformation());
    }

    public void sendHostKey() {
        //log("Send Host-Key");

        try {
            byte[] hostKey = HPatch3HostKey.getHostKey(hostOS);
            if (hostKey == null) {
                throw new Exception("Fail to get HostKey");
            }

            //S-PATCH3 Device-Key (MSB) 4ACE5144AB75DAF9B521DD10BE7C4D89 (LSB)
            final byte[] deviceKey = new byte[]{
                    (byte) 0x4A, (byte) 0xCE, (byte) 0x51, (byte) 0x44, (byte) 0xAB, (byte) 0x75, (byte) 0xDA, (byte) 0xF9,
                    (byte) 0xB5, (byte) 0x21, (byte) 0xDD, (byte) 0x10, (byte) 0xBE, (byte) 0x7C, (byte) 0x4D, (byte) 0x89,
            };

            HPatch3Cipher cipher = new HPatch3Cipher(deviceKey);
            byte[] encryptedHostKey = cipher.encrypt(hostKey);

            byte[] packetData = HPatch3PacketBuilder.builder.createSendHostKey(encryptedHostKey);

            sendPacket(packetData);
        } catch (Exception e) {
            //log(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void addSPatchTestObserver(HPatchTestObserver observer) {
        synchronized (hPatchTestObservers) {
            hPatchTestObservers.add(observer);
        }
    }

    @Override
    public void removeSPatchTestObserver(HPatchTestObserver observer) {
        synchronized (hPatchTestObservers) {
            hPatchTestObservers.remove(observer);
        }
    }

    @Override
    public OTA getBLE() {
        return null;
    }

    private OTA bpOTA;
    @Override
    public OTA getBP() {
        if (bpOTA == null) {
            bpOTA = new BPOTA(this, hostOS);
        }
        return bpOTA;
    }

    private OTAObserver bpOTAObserver;
    public void startBPOTA(OTAObserver observer) {
        bpOTAObserver = observer;

        changeState(FWUpdateState.name);
    }

    public void stopBPOTA() {
        bpOTAObserver = null;
    }

    public void logOTA(String message) {
        //log(message);
        if (bpOTAObserver != null) {
            bpOTAObserver.onOTAStateUpdated(message);
        }
    }

    private byte[] bpFW;
    public boolean readyBPFW() {
        try {
            sendECGStop();
        } catch (HPatchBLEException e) {
            e.printStackTrace();
        }

        String path = getBP().getTargetPath();
        boolean isValid = false;
        int n = path.lastIndexOf('/');
        String dir = path.substring(0, n);
        String fileName = path.substring(n + 1);
        try {
            bpFW = hostOS.restoreFile(dir, fileName, 0, 0);
            isValid = (bpFW != null);
        } catch (IOException e) {
            e.printStackTrace();
            //logOTA(e.getLocalizedMessage());
        }
        return isValid;
    }

    public byte[] getBPFWData() {
        if (bpFW == null) {
            readyBPFW();
        }
        return bpFW;
    }

    private int operationMode;

    @Override
    public void setOperationMode(int mode) {
        operationMode = mode;
    }

    @Override
    public int getOperationMode() {
        return operationMode;
    }

    @Override
    public void setTransferMode(int mode) {
        nextTransferModeValue = mode;
    }

    @Override
    public int getTransferMode() {
        return nextTransferModeValue;
    }

    private final List<HPatchMemoryAccessA8.ReadListener> memoryAccessA8ReadListeners = new ArrayList<>();

    @Override
    public void addReadA8Listener(ReadListener listener) {
        synchronized (memoryAccessA8ReadListeners) {
            memoryAccessA8ReadListeners.add(listener);
        }
    }

    @Override
    public void removeReadA8Listener(ReadListener listener) {
        synchronized (memoryAccessA8ReadListeners) {
            if (memoryAccessA8ReadListeners.contains(listener)) {
                memoryAccessA8ReadListeners.remove(listener);
            }
        }
    }

    @Override
    public void writeA8(int address, byte[] data) {
        sendPacket(HPatch3PacketBuilder.builder.createWriteA8(address, data));
    }

    @Override
    public void readA8(int address, int length) {
        sendPacket(HPatch3PacketBuilder.builder.createReadA8(address, length));
    }

    public void sendReset() {
        sendPacket(HPatch3PacketBuilder.builder.createReset());
    }

    private boolean isAccelerometerPacket(HPatch3Packet compositePacket) {
        HPatch3Packet sourcePacket = compositePacket.getFirstSubPacket(HPatchPacketType.Source);
        if (sourcePacket == null) {
            return false;
        } else {
            return (HPatchPacketSource.valueOf(sourcePacket.getPayloadAsInt8()) == HPatchPacketSource.Accelerometer);
        }
    }

    private AccelerometerInformation getAccelerometerInformation(HPatch3Packet packet) {
        int sequenceNumber = getSequenceNumber(packet);
        //log("Accelerometer Seq.Num: " + sequenceNumber);

        AccelerometerInformation information = null;

        if ((lastAccelerometerSequence + 1) == sequenceNumber) {
            lastAccelerometerSequence = sequenceNumber;

            MuAccelerometerInformation mu = new MuAccelerometerInformation(sequenceNumber);

            List<Integer> xValues = mu.getXValues();
            List<Integer> yValues = mu.getYValues();
            List<Integer> zValues = mu.getZValues();

            byte[] payload = packet.getFirstSubPacket(HPatchPacketType.Stream16Bit).getPayload();
            int count = payload.length / 6;
            int index = 0;
            for (int i = 0; i < count; i++) {
                int x = (short) ((payload[index] & 0xff) << 8) | (payload[index + 1] & 0xff);
                index += 2;
                xValues.add(x);

                int y = (short) ((payload[index] & 0xff) << 8) | (payload[index + 1] & 0xff);
                index += 2;
                yValues.add(y);

                int z = (short) ((payload[index] & 0xff) << 8) | (payload[index + 1] & 0xff);
                index += 2;
                zValues.add(z);
            }

            information = mu;
        }

        requestSignalTransfer();
        return information;
    }

    private boolean isGyroscopePacket(HPatch3Packet compositePacket) {
        HPatch3Packet sourcePacket = compositePacket.getFirstSubPacket(HPatchPacketType.Source);
        if (sourcePacket == null) {
            return false;
        } else {
            return (HPatchPacketSource.valueOf(sourcePacket.getPayloadAsInt8()) == HPatchPacketSource.Gyroscope);
        }
    }

    private GyroscopeInformation getGyroscopeInformation(HPatch3Packet packet) {
        int sequenceNumber = getSequenceNumber(packet);
        //log("Gyroscope Seq.Num: " + sequenceNumber);

        GyroscopeInformation information = null;

        if ((lastGyroscopeSequence + 1) == sequenceNumber) {
            lastGyroscopeSequence = sequenceNumber;

            MuGyroscopeInformation mu = new MuGyroscopeInformation(sequenceNumber);

            List<Integer> xValues = mu.getXValues();
            List<Integer> yValues = mu.getYValues();
            List<Integer> zValues = mu.getZValues();

            byte[] payload = packet.getFirstSubPacket(HPatchPacketType.Stream16Bit).getPayload();
            int count = payload.length / 6;
            int index = 0;
            for (int i = 0; i < count; i++) {
                int x = (short) ((payload[index] & 0xff) << 8) | (payload[index + 1] & 0xff);
                index += 2;
                xValues.add(x);

                int y = (short) ((payload[index] & 0xff) << 8) | (payload[index + 1] & 0xff);
                index += 2;
                yValues.add(y);

                int z = (short) ((payload[index] & 0xff) << 8) | (payload[index + 1] & 0xff);
                index += 2;
                zValues.add(z);
            }

            information = mu;
        }

        requestSignalTransfer();
        return information;
    }

    private boolean hasFallDetectPacket(HPatch3Packet packet) {
        return packet.getFirstSubPacket(HPatchPacketType.IMURet) != null;
    }

    private FallDetectInformation getFallDetectInformation(HPatch3Packet packet) {
        HPatch3Packet imuPacket = packet.getFirstSubPacket(HPatchPacketType.IMURet);
        byte[] imuData = imuPacket.getPayload();
        if (imuData.length < 4) {
            return null;
        }

        // 2017-06-26 Eunjae Hyun
        // [7:0]ResFall ==> [7]:FallAlarm, [6]:UFT, [5]:LFT, [4]:AfterFall, [3:0]:FallType

        boolean isFall = ((imuData[3] & 0x80) > 0);
        FallType fallType = FallType.FallingForwardOverSomething;

        MuFallDetectInformation fallDetectInformation = new MuFallDetectInformation(isFall, fallType);
        fallDetectInformation.setRawData(imuData);
        return fallDetectInformation;
    }

    private boolean hasPostureDetectPacket(HPatch3Packet packet) {
        return packet.getFirstSubPacket(HPatchPacketType.IMURet) != null;
    }

    private FileLogUtil fileLogger;
    private String fileLogName;
    private PostureDetectInformation getPostureDetectInformation(HPatch3Packet packet) {
        HPatch3Packet imuPacket = packet.getFirstSubPacket(HPatchPacketType.IMURet);
        byte[] imuData = imuPacket.getPayload();
        if (imuData.length < 4) {
            return null;
        }

        if (true) { //ToDo: Remove below - Only for Debugging
            if (fileLogger == null) {
                String path = Environment.getExternalStorageDirectory() + "/SPATCH_MU/";
                fileLogName = "" + getId() + "_" + TimeUtils.getDateTextForFile(System.currentTimeMillis()) + "_IMU.dat";
                fileLogger = new FileLogUtil(path);
            }

            //fileLogger.logging(fileLogName, imuData);
        }

        // [7:0]ResPos ==> [7]:Class [6:4]:Direction [3:0]PostureType
        // PostureType= 0: Sitting, 1: Lying, 2: Standing, 3: Walking
        int postureTypeData = (imuData[2] & 0x0F);

        PostureType postureType;
        switch (postureTypeData) {
            case 0:
                postureType = PostureType.Sitting;
                break;
            case 1:
                postureType = PostureType.Lying;
                break;
            case 2:
                postureType = PostureType.Standing;
                break;
            case 3:
                postureType = PostureType.Walking;
                break;
            default:
                postureType = PostureType.Standing;
                break;
        }

        MuPostureDetectInformation postureDetectInformation = new MuPostureDetectInformation(postureType);
        postureDetectInformation.setRawData(imuData);
        return postureDetectInformation;
    }

    private final List<FallDetectObserver> fallDetectObservers = new ArrayList<>();

    @Override
    public void addObserver(FallDetectObserver observer) {
        synchronized (fallDetectObservers) {
            fallDetectObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(FallDetectObserver observer) {
        synchronized (fallDetectObservers) {
            fallDetectObservers.remove(observer);
        }
    }

    private void broadcastFallDetect(FallDetectInformation information) {
        synchronized (fallDetectObservers) {
            for (FallDetectObserver observer : fallDetectObservers) {
                try {
                    observer.onFallDetected(information);
                } catch (Exception e) {
                    //log(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private final List<PostureDetectObserver> postureDetectObservers = new ArrayList<>();

    @Override
    public void addObserver(PostureDetectObserver observer) {
        synchronized (postureDetectObservers) {
            postureDetectObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(PostureDetectObserver observer) {
        synchronized (postureDetectObservers) {
            postureDetectObservers.remove(observer);
        }
    }

    private void broadcastPostureDetect(PostureDetectInformation information) {
        synchronized (postureDetectObservers) {
            for (PostureDetectObserver observer : postureDetectObservers) {
                try {
                    observer.onPostureDetected(information);
                } catch (Exception e) {
                    //log(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private final List<AccelerometerSensor.Observer> accelerometerObservers = new ArrayList<>();

    @Override
    public void addObserver(AccelerometerSensor.Observer observer) {
        synchronized (accelerometerObservers) {
            accelerometerObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(AccelerometerSensor.Observer observer) {
        synchronized (accelerometerObservers) {
            accelerometerObservers.remove(observer);
        }
    }

    private void broadcastAccelerometerUpdated(AccelerometerInformation information) {
        if (information != null) {
            synchronized (accelerometerObservers) {
                for (AccelerometerSensor.Observer observer : accelerometerObservers) {
                    try {
                        observer.onAccelerometerUpdated(information);
                    } catch (Exception e) {
                        //log(e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private final List<GyroscopeSensor.Observer> gyroscopeObservers = new ArrayList<>();

    @Override
    public void addObserver(GyroscopeSensor.Observer observer) {
        synchronized (gyroscopeObservers) {
            gyroscopeObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(GyroscopeSensor.Observer observer) {
        synchronized (gyroscopeObservers) {
            gyroscopeObservers.remove(observer);
        }
    }

    private void broadcastGyroscopeUpdated(GyroscopeInformation information) {
        if (information != null) {
            synchronized (gyroscopeObservers) {
                for (GyroscopeSensor.Observer observer : gyroscopeObservers) {
                    try {
                        observer.onGyroscopeUpdated(information);
                    } catch (Exception e) {
                        //log(e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private final List<SkinTemperatureSensor.Observer> skinTemperatureObservers = new ArrayList<>();

    @Override
    public void addObserver(SkinTemperatureSensor.Observer observer) {
        synchronized (skinTemperatureObservers) {
            skinTemperatureObservers.add(observer);
        }
    }

    @Override
    public void removeObserver(SkinTemperatureSensor.Observer observer) {
        synchronized (skinTemperatureObservers) {
            skinTemperatureObservers.remove(observer);
        }
    }

    private void broadcastSkinTemperatureUpdated(SkinTemperatureInformation information) {
        if (information != null) {
            synchronized (skinTemperatureObservers) {
                for (SkinTemperatureSensor.Observer observer : skinTemperatureObservers) {
                    try {
                        observer.onSkinTemperatureUpdated(information);
                    } catch (Exception e) {
                        //log(e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
