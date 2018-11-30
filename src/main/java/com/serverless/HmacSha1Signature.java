package com.serverless;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class HmacSha1Signature {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final char[] hexCode = "0123456789abcdef".toCharArray();

    public static String calculateRFC2104HMAC(String data, String key)
        throws NoSuchAlgorithmException, InvalidKeyException {

        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return printHexBinary(mac.doFinal(data.getBytes()));
    }

    private static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}