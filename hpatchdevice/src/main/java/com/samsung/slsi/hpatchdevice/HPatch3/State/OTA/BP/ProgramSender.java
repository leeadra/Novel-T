package com.samsung.slsi.hpatchdevice.HPatch3.State.OTA.BP;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;

public class ProgramSender {

    private final HPatch3Device device;

    public ProgramSender(HPatch3Device device) {
        this.device = device;
    }

    void send(byte[] data) {
        if (false) {
            device.sendPacket(data);
        } else {
            final int transferSize = 64 * 2;

            int i = 0;
            int dataLength = data.length;

            while (dataLength > 0) {
                int len = dataLength > transferSize ? transferSize : dataLength;
                byte[] d = new byte[len];
                System.arraycopy(data, i, d, 0, len);

                device.sendPacket(d);

                i += len;
                dataLength -= len;

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}