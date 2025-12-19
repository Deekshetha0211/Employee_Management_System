package com.grootan.ems.auth;

import com.grootan.ems.user.AppUser;
import com.grootan.ems.user.AppUserRepository;
import com.grootan.ems.user.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DbUserDetailsServiceTest {

    @Mock
    private AppUserRepository repository;

    @InjectMocks
    private DbUserDetailsService service;

    @Test
    void loadUser_returnsUserDetails() {
        AppUser user = new AppUser();
        user.setEmail("alice@example.com");
        user.setPasswordHash("hash");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);

        when(repository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("ALICE@example.com");

        assertThat(details.getUsername()).isEqualTo("alice@example.com");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUser_notFoundThrows() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@example.com"));
    }
}
