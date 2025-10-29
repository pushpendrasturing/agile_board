package com.agile.board.service;

import com.agile.board.domain.PasswordResetToken;
import com.agile.board.domain.Role;
import com.agile.board.domain.User;
import com.agile.board.repo.PasswordResetTokenRepository;
import com.agile.board.repo.UserRepository;
import com.agile.board.utils.TokenUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder encoder;
    private final Key key = Keys.hmacShaKeyFor("change-this-demo-secret-change-this-demo-secret".getBytes());
    private final Duration resetTtl = Duration.ofMinutes(15);

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


    /** create a short-lived, one-time reset token (returns plain token for now) */
    public String requestPasswordResetByEmailOrUsername(String emailOrUsername) {
        User u = users.findByUsername(emailOrUsername)
                .or(() -> users.findAll().stream().filter(x -> emailOrUsername.equalsIgnoreCase(x.getEmail())).findFirst())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        String plainToken = TokenUtil.generateToken();
        String tokenHash = TokenUtil.sha256Hex(plainToken);

        PasswordResetToken prt = PasswordResetToken.builder()
                .userId(u.getId())
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(resetTtl))
                .used(false)
                .build();
        resetTokens.save(prt);
        return plainToken; // In prod, email this instead of returning
    }

    /** validate token and set new password (consumes token) */
    public void resetPasswordWithToken(String plainToken, String newPassword) {
        String tokenHash = TokenUtil.sha256Hex(plainToken);
        PasswordResetToken prt = resetTokens.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("invalid token"));

        if (prt.isExpired()) throw new IllegalArgumentException("token expired");
        if (prt.isUsed())    throw new IllegalArgumentException("token already used");

        User u = users.findById(prt.getUserId()).orElseThrow(() -> new IllegalArgumentException("user missing"));

        u.setPasswordHash(encoder.encode(newPassword));
        users.save(u);

        prt.setUsed(true);
        resetTokens.save(prt);
    }

}