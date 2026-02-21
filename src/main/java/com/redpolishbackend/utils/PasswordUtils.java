package com.redpolishbackend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //Texto plano a hash BCrypt.
    public static String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    //Verifica texto plano con hash BCrypt.
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}

