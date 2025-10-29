package com.agile.board.service;

import com.agile.board.domain.Role;
import com.agile.board.domain.User;
import com.agile.board.repo.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final Key key = Keys.hmacShaKeyFor("change-this-demo-secret-change-this-demo-secret".getBytes());

    public User register(String username, String email, String password) {
        if (users.existsByUsername(username)) throw new IllegalArgumentException("username taken");
        User u = User.builder()
                .username(username)
                .email(email)
                .passwordHash(encoder.encode(password))
                .role("USER")
                .tokenVersion(0)
                .build();
        return users.save(u);
    }

    public String login(String username, String password) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("bad creds"));
        if (!encoder.matches(password, u.getPasswordHash()))
            throw new IllegalArgumentException("bad creds");

        var role = Role.from(u.getRole());
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(u.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                // embed role + token version (and optionally perms)
                .claim("role", role.name())
                .claim("ver", u.getTokenVersion())
                .claim("perms", role.permissions())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Key key() {
        return key;
    }
}