package com.samsung.slsi.hpatchdevice;

import android.support.test.runner.AndroidJUnit4;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatchPacketType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by ch36.park on 2017. 2. 27..
 */
@RunWith(AndroidJUnit4.class)
public class HPatch3PacketTest {

    @Test
    public void typeResponseTransferKey_must_be_hex42_Test() throws Exception {
        byte value = 0x42;
        HPatchPacketType type = HPatchPacketType.valueOf(value & 0xFF);
        assertEquals(0x42, type.getValue());
    }

    @Test
    public void typeReadResponse_must_be_hex0A_Test() throws Exception {
        byte value = 0x0A;
        HPatchPacketType type = HPatchPacketType.valueOf(value & 0xFF);
        assertEquals(0x0A, type.getValue());
    }

}
