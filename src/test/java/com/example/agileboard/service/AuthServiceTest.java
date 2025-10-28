package com.example.agileboard.service;

import com.example.agileboard.domain.User;
import com.example.agileboard.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    @InjectMocks AuthService authService;

    @Test
    void register_hashesPassword_and_saves() {
        when(users.existsByUsername("u")).thenReturn(false);
        when(encoder.encode("p")).thenReturn("HASH");
        when(users.save(any())).thenAnswer(a -> a.getArguments()[0]);

        User saved = authService.register("u","u@x.com","p");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(users).save(cap.capture());
        assertEquals("u", cap.getValue().getUsername());
        assertEquals("HASH", cap.getValue().getPasswordHash());
        assertEquals("USER", cap.getValue().getRole());
        assertNotNull(saved);
    }

    @Test
    void login_returnsJwt_onValidCreds() {
        User u = User.builder().id(1L).username("u").passwordHash("HASH").email("e@x.com").build();
        when(users.findByUsername("u")).thenReturn(Optional.of(u));
        when(encoder.matches("p","HASH")).thenReturn(true);

        String token = authService.login("u","p");
        assertNotNull(token);
    }

    @Test
    void login_throws_onBadCreds() {
        when(users.findByUsername("u")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.login("u","p"));
    }
}
