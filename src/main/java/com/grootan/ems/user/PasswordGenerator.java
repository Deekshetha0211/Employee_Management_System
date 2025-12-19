package com.grootan.ems.user;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private final SecureRandom RND = new SecureRandom();
    private static final String CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ" +
                    "abcdefghijkmnopqrstuvwxyz" +
                    "23456789" +
                    "!@#$%";

    public String generate(int len) {
        if (len <= 0) throw new IllegalArgumentException("length must be > 0");
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(RND.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
