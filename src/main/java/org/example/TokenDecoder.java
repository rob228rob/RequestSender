package org.example;

import java.util.Base64;

public class TokenDecoder {
    public static String encodeToken(String email, String code) {
        String token = email + ":" + code;

        return Base64.getEncoder().encodeToString(token.getBytes());
    }

    public static String decodeToken(String token) {
        return new String(Base64.getDecoder().decode(token));
    }
}