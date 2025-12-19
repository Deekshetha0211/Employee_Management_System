package com.grootan.ems.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        log.info("Configuring security filter chain");
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                // Swagger / OpenAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Auth
                .requestMatchers("/auth/**").permitAll()

                // Departments: ADMIN only
                .requestMatchers(HttpMethod.GET, "/api/departments/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                .requestMatchers(HttpMethod.POST, "/api/departments/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasAnyRole("ADMIN", "HR")

                // Employees: READ for all authenticated roles
                .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                // Employees: WRITE for ADMIN + HR_MANAGER
                .requestMatchers(HttpMethod.POST, "/api/employees/**").hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyRole("ADMIN", "HR", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAnyRole("ADMIN", "HR")

                // Everything else must be authenticated
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        log.info("Security filter chain configured");
        return http.build();
    }
}
