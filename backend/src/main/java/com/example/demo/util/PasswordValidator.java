package com.example.demo.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    /**
     * Validates password strength and returns a list of validation errors.
     * Returns an empty list if password is valid.
     *
     * Password requirements:
     * - Minimum 8 characters
     * - At least 1 uppercase letter (A-Z)
     * - At least 1 lowercase letter (a-z)
     * - At least 1 digit (0-9)
     * - At least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
     * - No spaces allowed
     */
    public List<String> validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
            return errors;
        }

        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter (A-Z)");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter (a-z)");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one digit (0-9)");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)");
        }

        if (WHITESPACE_PATTERN.matcher(password).find()) {
            errors.add("Password must not contain spaces");
        }

        return errors;
    }

    /**
     * Checks if the password is valid (meets all requirements)
     */
    public boolean isValid(String password) {
        return validate(password).isEmpty();
    }

    /**
     * Returns a formatted string with all password requirements
     */
    public String getRequirementsMessage() {
        return """
                Password must meet the following requirements:
                • At least 8 characters long
                • At least one uppercase letter (A-Z)
                • At least one lowercase letter (a-z)
                • At least one digit (0-9)
                • At least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
                • No spaces allowed
                """;
    }
}