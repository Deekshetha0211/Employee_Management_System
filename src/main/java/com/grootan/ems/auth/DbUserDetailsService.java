package com.grootan.ems.auth;

import com.grootan.ems.user.AppUser;
import com.grootan.ems.user.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserRepository repo;
    private static final Logger log = LoggerFactory.getLogger(DbUserDetailsService.class);

    public DbUserDetailsService(AppUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username.toLowerCase();
        log.debug("Loading user by email={}", email);
        AppUser u = repo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email={}", email);
                    return new UsernameNotFoundException("User not found");
                });

        return new User(
                u.getEmail(),
                u.getPasswordHash(),
                u.isEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }
}
