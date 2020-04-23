package com.samsung.slsi.hpatchdevice.HPatch3;

import com.samsung.slsi.HPatchHostOS;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

class HPatch3HostKey {

    private static byte[] generateHostKey() {
        byte[] key = null;
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            generator.init(128, random);
            Key secureKey = generator.generateKey();

            key = secureKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return key;
    }

    static byte[] getHostKey(HPatchHostOS hostOS) {
        byte[] key = null;

        if (hostOS != null) {
            String hostKeyString = hostOS.getPreference("HostKey");
            if (hostKeyString != null) {
                if (hostKeyString.length() == 32) {
                    //log("Stored-HostKey: " + hostKeyString);

                    key = new byte[16];
                    for (int i = 0; i < 16; i++) {
                        String hex = hostKeyString.substring(i * 2, i * 2 + 2);
                        key[i] = (byte) (Integer.parseInt(hex, 16) & 0xFF);
                    }
                }
            }
        }

        if (key == null) {
            key = generateHostKey();

            if (hostOS != null) {
                String keyText = "";
                for (byte b : key) {
                    keyText += String.format("%02X", b);
                }
                hostOS.setPreference("HostKey", keyText);
            }
        }

        if (key != null) {
            String keyText = "";
            for (byte b : key) {
                keyText += String.format("%02X", b);
            }
            //log("HostKey: " + keyText);
        }

        return key;
    }
}