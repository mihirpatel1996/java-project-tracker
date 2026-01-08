package com.example.demo.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodeGenerator {

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DEFAULT_CODE_LENGTH = 6;

    /**
     * Generates a random 6-character alphanumeric code.
     * Uses uppercase letters and digits only for better readability.
     * Example: "A3B9K2"
     */
    public String generateCode() {
        return generateCode(DEFAULT_CODE_LENGTH);
    }

    /**
     * Generates a random alphanumeric code of specified length.
     */
    public String generateCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            code.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }
        return code.toString();
    }
}