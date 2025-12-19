package com.grootan.ems.auth;

import com.grootan.ems.user.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiryMinutes;
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiry-minutes}") long expiryMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = expiryMinutes;
        log.info("Initialized JwtService with expiryMinutes={}", expiryMinutes);
    }

    public String generateToken(String subjectEmail, Role role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiryMinutes * 60);
        log.debug("Generating token for {} with role {} expiring at {}", subjectEmail, role, exp);

        return Jwts.builder()
                .subject(subjectEmail)
                .claims(Map.of("role", role.name()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        String email = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        log.debug("Extracted email {} from token", email);
        return email;
    }

    public Role extractRole(String token) {
        String role = (String) Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role");
        log.debug("Extracted role {} from token", role);
        return Role.valueOf(role);
    }
}
