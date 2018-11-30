package com.serverless;

import static com.serverless.HmacSha1Signature.calculateRFC2104HMAC;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

class HmacSha1SignatureTest {

    @Test
    public void testSignature() throws InvalidKeyException, NoSuchAlgorithmException {
        assertEquals("104152c5bfdca07bc633eebd46199f0255c9f49d", calculateRFC2104HMAC("data", "key"));
    }

}
