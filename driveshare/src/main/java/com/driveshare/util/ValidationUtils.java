package com.driveshare.util;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
