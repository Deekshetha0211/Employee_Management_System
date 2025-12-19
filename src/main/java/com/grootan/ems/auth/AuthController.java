package com.grootan.ems.auth;

import com.grootan.ems.auth.dto.LoginRequest;
import com.grootan.ems.auth.dto.LoginResponse;
import com.grootan.ems.user.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final AppUserRepository userRepo;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authManager, JwtService jwtService, AppUserRepository userRepo) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        log.info("Login attempt for {}", email);
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.getPassword()));

        var user = userRepo.findByEmail(email).orElseThrow(); // should exist if auth passed
        String token = jwtService.generateToken(email, user.getRole());

        log.info("Login successful for {}", email);
        return new LoginResponse(token);
    }
}
