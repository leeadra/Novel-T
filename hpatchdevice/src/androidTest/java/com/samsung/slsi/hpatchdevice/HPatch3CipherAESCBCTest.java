package com.samsung.slsi.hpatchdevice;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Cipher;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class HPatch3CipherAESCBCTest {

    private static byte[] SampleKeyBytes = new byte[] {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
    };

    private static byte[] SampleInitialVector = new byte[]{
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
    };

    private static byte[] SamplePlainBytes = new byte[]{
            (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0D, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0C, (byte) 0x08, (byte) 0x0C,
            (byte) 0x08, (byte) 0x0D, (byte) 0x08, (byte) 0x0D, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0D, (byte) 0x08, (byte) 0x0D, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x0D, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0E,
            (byte) 0x08, (byte) 0x0D, (byte) 0x08, (byte) 0x0C, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0F,

            (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0B, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0E,
            (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0D,

            (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x0F,
            (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x12,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0E, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x11,
            (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x0F, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11,

            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x12,
            (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x10, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x12,
            (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x13, (byte) 0x08, (byte) 0x10,
            (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x13, (byte) 0x08, (byte) 0x12,
            (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x13, (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x11,
            (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x14, (byte) 0x08, (byte) 0x13, (byte) 0x08, (byte) 0x12,
            (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x12,
            (byte) 0x08, (byte) 0x13, (byte) 0x08, (byte) 0x12, (byte) 0x08, (byte) 0x11, (byte) 0x08, (byte) 0x11,
    };

    private static byte[] SampleEncryptedBytes = new byte[]{
            (byte) 0x91, (byte) 0x8D, (byte) 0x32, (byte) 0x5E, (byte) 0x95, (byte) 0x11, (byte) 0x68, (byte) 0xA9,
            (byte) 0xA6, (byte) 0x0B, (byte) 0x4A, (byte) 0xF3, (byte) 0x64, (byte) 0x8E, (byte) 0x43, (byte) 0xBF,
            (byte) 0xDC, (byte) 0x97, (byte) 0x46, (byte) 0x97, (byte) 0x84, (byte) 0xBE, (byte) 0xD4, (byte) 0x44,
            (byte) 0x60, (byte) 0x48, (byte) 0xC9, (byte) 0xFF, (byte) 0x36, (byte) 0x80, (byte) 0x4D, (byte) 0xA5,
            (byte) 0x82, (byte) 0x09, (byte) 0x78, (byte) 0x98, (byte) 0x69, (byte) 0x0D, (byte) 0xBE, (byte) 0x04,
            (byte) 0x82, (byte) 0x44, (byte) 0xF8, (byte) 0x36, (byte) 0xDE, (byte) 0x6E, (byte) 0x19, (byte) 0x74,
            (byte) 0xE9, (byte) 0x83, (byte) 0x48, (byte) 0xD1, (byte) 0x7F, (byte) 0xA6, (byte) 0x7C, (byte) 0x11,
            (byte) 0x54, (byte) 0x25, (byte) 0xE8, (byte) 0xD5, (byte) 0xB5, (byte) 0x50, (byte) 0x8C, (byte) 0xB9,

            (byte) 0xA7, (byte) 0xD7, (byte) 0xEF, (byte) 0x23, (byte) 0x32, (byte) 0xDF, (byte) 0xB6, (byte) 0x68,
            (byte) 0x74, (byte) 0xC1, (byte) 0x8A, (byte) 0x11, (byte) 0xB2, (byte) 0x61, (byte) 0xD7, (byte) 0x80,
            (byte) 0x18, (byte) 0x57, (byte) 0xC0, (byte) 0x7D, (byte) 0xC0, (byte) 0x13, (byte) 0x7A, (byte) 0x40,
            (byte) 0x37, (byte) 0xB2, (byte) 0xD6, (byte) 0xD9, (byte) 0xD9, (byte) 0x35, (byte) 0x38, (byte) 0x6A,
            (byte) 0xE4, (byte) 0x86, (byte) 0x85, (byte) 0xE2, (byte) 0x5F, (byte) 0xDA, (byte) 0x45, (byte) 0x63,
            (byte) 0xA8, (byte) 0x39, (byte) 0xA1, (byte) 0x37, (byte) 0x16, (byte) 0xB2, (byte) 0x1E, (byte) 0xC8,
            (byte) 0xEC, (byte) 0xA0, (byte) 0x00, (byte) 0x42, (byte) 0x34, (byte) 0xD5, (byte) 0x33, (byte) 0x4E,
            (byte) 0xFB, (byte) 0xED, (byte) 0x2B, (byte) 0x0D, (byte) 0x8F, (byte) 0x12, (byte) 0x98, (byte) 0xAC,

            (byte) 0xFA, (byte) 0x97, (byte) 0x61, (byte) 0x28, (byte) 0x99, (byte) 0x6D, (byte) 0xD3, (byte) 0x96,
            (byte) 0xAC, (byte) 0xE8, (byte) 0x80, (byte) 0xE6, (byte) 0x32, (byte) 0xA0, (byte) 0xE4, (byte) 0xDA,
            (byte) 0x01, (byte) 0x55, (byte) 0x4E, (byte) 0x33, (byte) 0xB0, (byte) 0x26, (byte) 0x25, (byte) 0x2C,
            (byte) 0x9C, (byte) 0x8E, (byte) 0x32, (byte) 0x9C, (byte) 0x72, (byte) 0x42, (byte) 0x1C, (byte) 0x0A,
            (byte) 0x7B, (byte) 0xE8, (byte) 0xD3, (byte) 0x87, (byte) 0xEC, (byte) 0xBC, (byte) 0xE4, (byte) 0x3D,
            (byte) 0x53, (byte) 0xA2, (byte) 0x5B, (byte) 0xA5, (byte) 0xDA, (byte) 0xE5, (byte) 0x09, (byte) 0xDA,
            (byte) 0xFA, (byte) 0x26, (byte) 0xC9, (byte) 0xA9, (byte) 0x7C, (byte) 0x42, (byte) 0x9F, (byte) 0x60,
            (byte) 0x84, (byte) 0xEB, (byte) 0x2C, (byte) 0x99, (byte) 0x13, (byte) 0xDB, (byte) 0x69, (byte) 0xAE,

            (byte) 0x78, (byte) 0xE9, (byte) 0x91, (byte) 0xD6, (byte) 0x70, (byte) 0x1E, (byte) 0x5E, (byte) 0xCA,
            (byte) 0x20, (byte) 0x06, (byte) 0xFA, (byte) 0xDA, (byte) 0x08, (byte) 0x5F, (byte) 0x32, (byte) 0x80,
            (byte) 0xB8, (byte) 0xE0, (byte) 0xD2, (byte) 0x9B, (byte) 0x22, (byte) 0x1C, (byte) 0x35, (byte) 0x98,
            (byte) 0x9F, (byte) 0xF2, (byte) 0x54, (byte) 0x42, (byte) 0xFB, (byte) 0xF9, (byte) 0xA3, (byte) 0x27,
            (byte) 0x76, (byte) 0xF7, (byte) 0xD5, (byte) 0xF0, (byte) 0x5D, (byte) 0xF8, (byte) 0x2A, (byte) 0x3B,
            (byte) 0x8E, (byte) 0xEC, (byte) 0x10, (byte) 0xEC, (byte) 0x4E, (byte) 0xB9, (byte) 0x27, (byte) 0x1C,
            (byte) 0xDF, (byte) 0x1A, (byte) 0x78, (byte) 0x09, (byte) 0x35, (byte) 0xA4, (byte) 0x74, (byte) 0xEF,
            (byte) 0xC4, (byte) 0xAD, (byte) 0x14, (byte) 0x7B, (byte) 0xAF, (byte) 0x2B, (byte) 0x50, (byte) 0x17,
    };

/*
    @Test
    public void encryptTest() throws Exception {
        HPatch3Cipher cipher = new HPatch3Cipher(SampleKeyBytes, SampleInitialVector);

        byte[] encryptedBytes = cipher.encrypt(SamplePlainBytes);

        for (int i = 0; i < SampleEncryptedBytes.length; i++) {
            assertEquals("" + i + "th Sample is different", SampleEncryptedBytes[i], encryptedBytes[i]);
        }
        assertEquals(SampleEncryptedBytes.length, encryptedBytes.length);

        assertArrayEquals(SampleEncryptedBytes, encryptedBytes);
    }

    @Test
    public void decryptTest() throws Exception {
        HPatch3Cipher cipher = new HPatch3Cipher(SampleKeyBytes, SampleInitialVector);

        byte[] decryptedBytes = cipher.decrypt(SampleEncryptedBytes);

        for (int i = 0; i < SamplePlainBytes.length; i++) {
            assertEquals("" + i + "th Sample is different", SamplePlainBytes[i], decryptedBytes[i]);
        }
        assertEquals(SamplePlainBytes.length, decryptedBytes.length);

        assertArrayEquals(SamplePlainBytes, decryptedBytes);
    }
*/
    @Test
    public void encryptAndDecryptTest() throws Exception {
        HPatch3Cipher cipher = new HPatch3Cipher(SampleKeyBytes, SampleInitialVector);

        byte[] encryptedBytes = cipher.encrypt(SamplePlainBytes);

//        assertArrayEquals(SampleEncryptedBytes, encryptedBytes);

        String txt = "EncryptedBytes: ";
        for (byte b : encryptedBytes) {
            txt += String.format("%02X", b);
        }
        Log.d("HPatch3CipherAESCBCTest", txt);

        byte[] decryptedBytes = cipher.decrypt(encryptedBytes);

        assertArrayEquals(SamplePlainBytes, decryptedBytes);
    }
}
