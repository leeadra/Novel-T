package com.samsung.slsi.hpatchdevice.HPatch3;

import java.io.ByteArrayOutputStream;

/**
 * Created by ch36.park on 2017. 5. 17..
 */

public class BPOTAPacketBuilder {

    private static final byte[] SyncStartTag = new byte[]{
            (byte) 0x55, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF,    //Sync Start
    };
    private static final byte[] SyncEndTag = new byte[]{
            (byte) 0x44, (byte) 0x99, (byte) 0xEE, (byte) 0xEE,    //Sync End
    };

    public static BPOTAPacketBuilder builder = new BPOTAPacketBuilder();

    private BPOTAPacketBuilder() {
    }

    public byte[] createStartPacket() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            stream.write(SyncStartTag);
            stream.write(new byte[]{
                    (byte) HPatchPacketType.BP_OTA_Start.getValue(), (byte) 0x00, (byte) 0x00
            });
            stream.write(SyncEndTag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    public byte[] createStopPacket() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            stream.write(SyncStartTag);
            stream.write(new byte[]{
                    (byte) HPatchPacketType.BP_OTA_Stop.getValue(), (byte) 0x00, (byte) 0x00
            });
            stream.write(SyncEndTag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    public byte[] createDoingPacket(byte[] bootRomPacket) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            stream.write(SyncStartTag);
            stream.write(new byte[]{
                    (byte) HPatchPacketType.BP_OTA_Doing.getValue(),
                    (byte) (bootRomPacket.length & 0xFF),
                    (byte) ((bootRomPacket.length >> 8) & 0xFF)
            });
            stream.write(bootRomPacket);
            stream.write(SyncEndTag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    public byte[] createReadIDPacket() {
        return createDoingPacket(BPBootROMPacketBuilder.builder.createReadDeviceIDPacket());
    }

    public byte[] createReadModePacket() {
        return createDoingPacket(BPBootROMPacketBuilder.builder.createReadDeviceModePacket());
    }

    public byte[] createEraseFlashPacket() {
        return createDoingPacket(BPBootROMPacketBuilder.builder.createEraseFlashPacket());
    }

    public byte[] createProgramPacket(int address, byte[] data) {
        return createDoingPacket(BPBootROMPacketBuilder.builder.createProgramPacket(address, data));
    }
}
