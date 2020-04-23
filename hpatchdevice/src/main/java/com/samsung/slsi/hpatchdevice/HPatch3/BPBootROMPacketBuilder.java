package com.samsung.slsi.hpatchdevice.HPatch3;

import java.io.ByteArrayOutputStream;
import java.security.InvalidParameterException;

/**
 * Created by ch36.park on 2017. 5. 17..
 */

public class BPBootROMPacketBuilder {

    private static final byte[] SyncStartTag = new byte[]{
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,    //Sync Start
    };
    private static final byte[] SyncEndTag = new byte[]{
            (byte) 0x7F, (byte) 0x7F, (byte) 0x7F, (byte) 0x7F,    //Sync End
    };

    public static final byte BP_BOOTROM_CMD_READ = (byte) 0x0A;
    public static final byte BP_BOOTROM_CMD_WRITE = (byte) 0x0B;
    public static final byte BP_BOOTROM_CMD_READ_RESPONSE = (byte) 0x08;
    public static final byte BP_BOOTROM_CMD_ERASE_FLASH = (byte) 0x0C;
    public static final byte BP_BOOTROM_CMD_PROGRAM_FLASH = (byte) 0x0D;

    public static final byte BP_BOOTROM_ADDR_DEFAULT = (byte) 0x00;
    public static final byte BP_BOOTROM_ADDR_DEVICE_ID = (byte) 0x00;
    public static final byte BP_BOOTROM_ADDR_DEVICE_MODE = (byte) 0x12;

    public static final byte BP_BOOTROM_FW_CMD_ERASE = (byte) 0xD8;
    public static final byte BP_BOOTROM_FW_CMD_PROGRAM = (byte) 0xD9;

    public static BPBootROMPacketBuilder builder = new BPBootROMPacketBuilder();

    private BPBootROMPacketBuilder() {
    }

    public byte[] createReadDeviceIDPacket() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            stream.write(SyncStartTag);
            stream.write(new byte[]{
                    (byte) 0x04, (byte) 0x00,
                    BP_BOOTROM_CMD_READ,
                    BP_BOOTROM_ADDR_DEVICE_ID,
                    (byte) 0x00, (byte) 0x00
            });
            stream.write(SyncEndTag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    public byte[] createReadDeviceModePacket() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            stream.write(SyncStartTag);
            stream.write(new byte[]{
                    (byte) 0x04, (byte) 0x00,
                    BP_BOOTROM_CMD_READ,
                    BP_BOOTROM_ADDR_DEVICE_MODE,
                    (byte) 0x00, (byte) 0x00
            });
            stream.write(SyncEndTag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    public byte[] createEraseFlashPacket() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            int eraseAddress = 0x0020;
            byte upi = (byte)((eraseAddress >> 8) & 0xFF);
            byte lpi = (byte)(eraseAddress & 0xFF);

            int eraseSize = 8159;
            byte upn = (byte)((eraseSize >> 8) & 0xFF);
            byte lpn = (byte)(eraseSize & 0xFF);

            byte checkSum = (byte)(upi + lpi + upn + lpn);

            stream.write(SyncStartTag);
            stream.write(new byte[]{
                    //length
                    (byte) 0x0A, (byte) 0x00,

                    //PAYLOAD
                    BP_BOOTROM_CMD_ERASE_FLASH, //CMD
                    BP_BOOTROM_ADDR_DEFAULT,    //ADDR
                    (byte) 0x00, (byte) 0x00,   //DATA
                    BP_BOOTROM_FW_CMD_ERASE,    //FW_CMD
                    upi, lpi,                   //ERASE_ADDR 0x0020 = 32
                    upn, lpn,                   //ERASE_SIZE 0x1FDF = 8159
                    checkSum                    //CHECK_SUM
            });
            stream.write(SyncEndTag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    public byte[] createProgramPacket(int programAddress, byte[] programData) {
        if (programData.length != 256) {
            throw new InvalidParameterException("Invalid Program Data Length");
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            byte upi = (byte)((programAddress >> 8) & 0xFF);
            byte lpi = (byte)(programAddress & 0xFF);

            byte checkSum = (byte)(upi + lpi);
            for (byte b : programData) {
                checkSum += b;
            }

            stream.write(SyncStartTag);
            stream.write(new byte[]{
                            //length
                            (byte) 0x08, (byte) 0x01,

                            //PAYLOAD
                            BP_BOOTROM_CMD_PROGRAM_FLASH,   //CMD
                            BP_BOOTROM_ADDR_DEFAULT,        //ADDR
                            (byte) 0x00, (byte) 0x00,       //DATA
                            BP_BOOTROM_FW_CMD_PROGRAM,      //FW_CMD
                            upi, lpi,                       //PROGRAM_ADDR 0x0020 = 32
                    });
            stream.write(programData);
            stream.write(new byte[] {
                    checkSum                                //CHECK_SUM
            });
            stream.write(SyncEndTag);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }
}
