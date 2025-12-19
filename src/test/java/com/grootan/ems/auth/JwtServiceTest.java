package com.grootan.ems.auth;

import com.grootan.ems.user.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generateAndExtractToken() {
        String secret = "01234567890123456789012345678901"; // 32 chars for HS256
        JwtService jwtService = new JwtService(secret, 60);

        String token = jwtService.generateToken("alice@example.com", Role.ADMIN);

        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.ADMIN);
    }
}
