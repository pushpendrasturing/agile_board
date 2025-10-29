package com.agile.board.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public final class TokenUtil {
    private static final SecureRandom RAND = new SecureRandom();

    private TokenUtil() {}

    public static String generateToken() {
        // random UUID + 16 random bytes -> base64 (URL-safe, no padding)
        byte[] rand = new byte[16];
        RAND.nextBytes(rand);
        String mixed = UUID.randomUUID() + "-" + Base64.getUrlEncoder().withoutPadding().encodeToString(rand);
        return mixed;
    }

    /** Hex-encoded SHA-256 */
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Hash failure", e);
        }
    }
}
