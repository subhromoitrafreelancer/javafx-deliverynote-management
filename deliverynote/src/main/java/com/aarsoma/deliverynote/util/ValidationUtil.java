package com.aarsoma.deliverynote.util;

// ValidationUtil.java

public class ValidationUtil {

    public static boolean isValidNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email is optional
        }

        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }

        return phone.matches("^[0-9\\+\\-\\s]+$");
    }
}
