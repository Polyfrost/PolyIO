package cc.polyfrost.polyio.util;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * @author xtrm
 */
public class PolyHashing {
    public static final MessageDigest SHA512 = findDigest("SHA-512");
    public static final MessageDigest SHA256 = findDigest("SHA-256");
    public static final MessageDigest SHA1 = findDigest("SHA-1");
    public static final MessageDigest MD5 = findDigest("MD5");

    public static String hash(String string, MessageDigest digest) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        byte[] hashedBytes = hash(bytes, digest);
        BigInteger bigInt = new BigInteger(1, hashedBytes);
        return bigInt.toString(16);
    }

    public static byte[] hash(byte[] bytes, MessageDigest digest) {
        return digest.digest(bytes);
    }

    public static String hash(Path path, MessageDigest messageDigest) throws IOException {
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(hash(fileBytes, messageDigest), StandardCharsets.UTF_8);
    }

    public static MessageDigest findDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
