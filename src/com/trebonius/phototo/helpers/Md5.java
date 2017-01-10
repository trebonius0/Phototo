package com.trebonius.phototo.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Md5 {

    public static String encodeString(String str) {
        byte[] uniqueKey = str.getBytes();
        byte[] hash = null;

        try {
            hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("No MD5 support in this VM.");
        }

        StringBuilder hashString = new StringBuilder(40);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }
        return hashString.toString();
    }

    private static String getHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            if (b <= 0x0F && b >= 0x00) { // On rajoute le 0 de poid fort ignoré à la conversion.  
                sb.append('0');
            }
            sb.append(String.format("%x", b));
        }
        return sb.toString();
    }
}
