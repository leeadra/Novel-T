package com.samsung.slsi.hpatchdevice;

import com.samsung.slsi.hpatchdevice.HPatch3.BPOTACRC;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by ch36.park on 2017. 5. 19..
 */

public class BPOTACRCTest {
    @Test
    public void crc_sample_test() throws Exception {
        byte[] sample = new byte[]{
                (byte) 0x00, (byte) 0x20, (byte) 0x03, (byte) 0x20, (byte) 0xA9, (byte) 0x23, (byte) 0x00, (byte) 0x00,
                (byte) 0xBF, (byte) 0x69, (byte) 0x00, (byte) 0x00, (byte) 0x5B, (byte) 0x24, (byte) 0x00, (byte) 0x00,
        };
        byte[] expectedResult = new byte[]{
                (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xE7, (byte) 0x28, (byte) 0x0F, (byte) 0x9C,
        };

        byte[] actualResult = BPOTACRC.crc.create(sample);

        assertEquals(expectedResult.length, actualResult.length);
        for (int i = 0; i < expectedResult.length; i++) {
            assertEquals(expectedResult[i], actualResult[i]);
        }
    }
}
