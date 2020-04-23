package com.samsung.slsi.hpatchdevice.HPatch3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;

/**
 * Created by ch36.park on 2017. 5. 19..
 */

public class HPatch3PacketBuilder {
    private static final int typeSize = 1;
    private static final int lengthSize = 2;

    private static final byte[] SyncStartTag = new byte[]{
            (byte) 0x55, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF,    //Sync Start
    };
    private static final byte[] SyncEndTag = new byte[]{
            (byte) 0x44, (byte) 0x99, (byte) 0xEE, (byte) 0xEE,    //Sync End
    };

    public static HPatch3PacketBuilder builder = new HPatch3PacketBuilder();

    private HPatch3PacketBuilder() {
    }

    private byte[] createPacket(byte[] payload) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            stream.write(SyncStartTag);
            stream.write(payload);
            stream.write(SyncEndTag);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stream.toByteArray();
    }

    public byte[] createECGStart(byte ecgOperationMode) {
        return createPacket(new byte[] {
                    (byte) HPatchPacketType.Write1ByteAddress.getValue(), (byte)0x04, (byte)0x00, (byte)0x10, (byte)0x01, (byte)0x00, (byte) ecgOperationMode, //Start-ECG
        });
    }

    public byte[] createECGStop() {
        return createPacket(new byte[] {
                    (byte) HPatchPacketType.Write1ByteAddress.getValue(), (byte)0x04, (byte)0x00, (byte)0x10, (byte)0x01, (byte)0x00, (byte) 0x00, //Stop-ECG
        });
    }

    public byte[] createKeepAlive() {
        return createPacket(new byte[] {
                (byte) HPatchPacketType.KeepAlive.getValue(), (byte) 0x00, (byte) 0x00,     //Keep-Alive
        });
    }

    public byte[] createRequestDeviceInformation() {
        return createPacket(new byte[] {
                (byte) HPatchPacketType.Read1ByteAddress.getValue(), (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x0A, (byte)0x00, //READ_A8 (Addr: 0x00 ~ [10 bytes])
        });
    }

    public byte[] createSendHostKey(byte[] hostKey) {
        byte[] packetData = createPacket(new byte[] {
                (byte) HPatchPacketType.RegisterHostKey.getValue(),    // Register Host Key
                (byte) 0x10, (byte) 0x00,   //Length

                //Host Key
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
                (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
        });
        System.arraycopy(hostKey, 0, packetData, 7, hostKey.length);    //replace real hostkey
        return packetData;
    }

    public byte[] createRequestECGSignal(int startSequenceNumber, int packetCount) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 4 + 1).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(new byte[]{
                (byte) HPatchPacketType.DataRequest.getValue(), (byte) 0x05, (byte) 0x00,
        });
        buffer.putInt(startSequenceNumber); // 4 bytes
        buffer.put((byte) packetCount);  // 1 byte

        return createPacket(buffer.array());
    }

    public byte[] createRequestSignal(int startSequenceNumber, int packetCount, HPatchPacketSource source) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 4 + 1 + 1).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(new byte[]{
                (byte) HPatchPacketType.DataRequest.getValue(), (byte) 0x06, (byte) 0x00,
        });
        buffer.putInt(startSequenceNumber);     // 4 bytes
        buffer.put((byte) packetCount);         // 1 byte
        buffer.put((byte) source.getValue());   // 1 byte

        return createPacket(buffer.array());
    }

    public byte[] createWriteA8(int address, byte[] data) {
        final int addressSize = 1;
        final int dataLengthSize = 2;
        final int dataSize = data.length;
        final int limitSize = 0x10000;
        final int totalPacketSize = addressSize + dataLengthSize + dataSize;

        if ((typeSize + lengthSize + totalPacketSize) >= limitSize) {
            throw new InvalidParameterException("data length (" + data.length + ") is too long.");
        }
        byte[] packet = new byte[typeSize + lengthSize + totalPacketSize];
        packet[0] = (byte) HPatchPacketType.Write1ByteAddress.getValue();
        packet[1] = (byte)(totalPacketSize & 0xFF);
        packet[2] = (byte)((totalPacketSize >> 8) & 0xFF);
        packet[3] = (byte)(address & 0xFF);
        packet[4] = (byte)(dataSize & 0xFF);
        packet[5] = (byte)((dataSize >> 8) & 0xFF);
        for (int i = 0; i < dataSize; i++) {
            packet[6 + i] = data[i];
        }
        System.arraycopy(packet, 6, data, 0, dataSize);

        return createPacket(packet);
    }

    public byte[] createReadA8(int address, int dataSize) {
        final int addressLengthSize = 1;
        final int addressSize = 1;
        final int dataLengthSize = 2;
        final int limitSize = 0x10000;

        final int totalPacketSize = addressSize + dataLengthSize;
        final int totalResponsePacketSize = addressLengthSize + addressSize + dataLengthSize + dataSize;

        if ((typeSize + lengthSize + totalResponsePacketSize) >= limitSize) {
            throw new InvalidParameterException("data length (" + dataSize+ ") is too long.");
        }
        byte[] packet = new byte[typeSize + lengthSize + addressSize + dataLengthSize];
        packet[0] = (byte) HPatchPacketType.Read1ByteAddress.getValue();
        packet[1] = (byte)(totalPacketSize & 0xFF);
        packet[2] = (byte)((totalPacketSize >> 8) & 0xFF);
        packet[3] = (byte)(address & 0xFF);
        packet[4] = (byte)(dataSize & 0xFF);
        packet[5] = (byte)((dataSize >> 8) & 0xFF);

        return createPacket(packet);
    }

    public byte[] createReset() {
        return createPacket(new byte[] {
                (byte) HPatchPacketType.SWReset.getValue(),    // Reset
                (byte) 0x00, (byte) 0x00,   //Length
        });
    }
}
