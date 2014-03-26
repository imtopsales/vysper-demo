package com.imtopsales.vysper.util;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;


public final class RSAUtil {
    private RSAUtil() {}
    
    static final int ENCRYPT_LEN = 117;
    static final int DECRYPT_LEN = 256;
    
    private static String bytes2Hex(byte[] bytes) {
        StringBuilder buff = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() < 2) {
                buff.append('0');
            }
            buff.append(hex);
        }
        return buff.toString();
    }
    
    private static byte[] hex2Bytes(String number) {
        byte[] bytes = new byte[number.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(number.substring(i * 2, (i + 1) * 2), 16);
        }
        return bytes;
    }
    
    private static Key hexKey2Key(boolean isPrivate, String hexKey) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            
            if (isPrivate) {
                return factory.generatePrivate(new PKCS8EncodedKeySpec(hex2Bytes(hexKey)));
            } else {
                return factory.generatePublic(new X509EncodedKeySpec(hex2Bytes(hexKey)));
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private static byte[] string2Utf8Bytes(String string) {
        if (null == string) {
            return null;
        }
        try {
            return string.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    private static String bytes2Utf8String(byte[] bytes) {
        if (null == bytes || bytes.length < 1) {
            return null;
        }
        
        try {
            return new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    private static Cipher newCipher(int opmode, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(opmode, key);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static String encryptWithPriKey(String priKey, String data) {
        try {
            Cipher cipher = newCipher(Cipher.ENCRYPT_MODE, hexKey2Key(true, priKey));
            
            StringBuilder dataBuff = new StringBuilder();
            int index = 0;
            while (index < data.length()) {
                
                String encData;
                int end = index + ENCRYPT_LEN;
                if (end < data.length()) {
                    encData = data.substring(index, end);
                } else {
                    encData = data.substring(index);
                }
                index = end;
                
                dataBuff.append(bytes2Hex(cipher.doFinal(string2Utf8Bytes(encData))));
            }
            
            return dataBuff.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static String decryptWithPubKey(String pubKey, String data) {
        try {
            Cipher cipher = newCipher(Cipher.DECRYPT_MODE, hexKey2Key(false, pubKey));
            
            StringBuilder dataBuff = new StringBuilder();
            for (int i = 0, max = data.length() / DECRYPT_LEN; i < max; i++) {
                String subData = data.substring(i * DECRYPT_LEN, (i+1) * DECRYPT_LEN);
                dataBuff.append(bytes2Utf8String(cipher.doFinal(hex2Bytes(subData))));
            }
            
            return dataBuff.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static String generateSigature(String priKey, String src) {
        try {
            Signature sigEng = Signature.getInstance("SHA1withRSA");
            byte[] pribyte = hex2Bytes(priKey.trim());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pribyte);

            KeyFactory fac = KeyFactory.getInstance("RSA");

            RSAPrivateKey privateKey = (RSAPrivateKey) fac
                    .generatePrivate(keySpec);
            sigEng.initSign(privateKey);
            sigEng.update(string2Utf8Bytes(src));

            byte[] signature = sigEng.sign();
            return bytes2Hex(signature);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static void generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            
            SecureRandom random = new SecureRandom();
            random.setSeed(System.currentTimeMillis());
            generator.initialize(1024, random);
            KeyPair keyPair = generator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            
//            System.out.println(publicKey.getModulus());
//            System.out.println(publicKey.getPublicExponent());
//            System.out.println();
//            System.out.println(publicKey.getModulus().toString(16));
//            System.out.println(publicKey.getPublicExponent().toString(16));
//            System.out.println();
//            System.out.println(privateKey.getPrivateExponent());
//            
//            System.out.println();
            
            System.out.println("Public:\r\n"
                    + bytes2Hex(publicKey.getEncoded()));
            System.out.println();
            System.out.println("Private:\r\n"
                    + bytes2Hex(privateKey.getEncoded()));
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static void main(String[] args) {
        generateKeyPair();
    }
}
