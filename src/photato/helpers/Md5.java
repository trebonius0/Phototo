package photato.helpers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Md5 {

    public static String encodeString(String str) {
        if (str == null) {
            throw new IllegalArgumentException();
        }
        byte[] hash = null;

        try {
            hash = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF8"));
        } catch (NoSuchAlgorithmException e) {
            throw new Error("No MD5 support in this VM.");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Unsupported encoding");
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
}
