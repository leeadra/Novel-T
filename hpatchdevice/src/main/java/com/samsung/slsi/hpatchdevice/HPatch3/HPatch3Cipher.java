package com.samsung.slsi.hpatchdevice.HPatch3;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ch36.park on 2017. 2. 14..
 */

public class HPatch3Cipher {

    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public byte[] key;

    public HPatch3Cipher(byte[] keyBytes) throws Exception {
        final String cipherTransformation = "AES/ECB/NoPadding";
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        key = keyBytes;

        encryptCipher = Cipher.getInstance(cipherTransformation);
        encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);

        decryptCipher = Cipher.getInstance(cipherTransformation);
        decryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
    }

    public HPatch3Cipher(byte[] keyBytes, byte[] initialVectorBytes) throws Exception {
        final String cipherTransformation = "AES/CBC/NoPadding";
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVectorBytes);

        encryptCipher = Cipher.getInstance(cipherTransformation);
        encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        decryptCipher = Cipher.getInstance(cipherTransformation);
        decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
    }

    public byte[] decrypt(byte[] data) throws Exception
    {
        return decryptCipher.doFinal(data);
    }

    public byte[] encrypt(byte[] data) throws Exception
    {
        return encryptCipher.doFinal(data);
    }
}
