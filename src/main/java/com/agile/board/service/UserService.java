package com.agile.board.service;

import com.agile.board.domain.User;
import com.agile.board.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Transactional
    public User updateProfile(String username, String name, String email, String newPassword, String currentPassword) {
        User u = users.findByUsername(username).orElseThrow();
        if (email != null && !email.isBlank()) u.setEmail(email);
        if (name != null && !name.isBlank()) u.setName(name);
        if (newPassword != null && !newPassword.isBlank()) {
            if (currentPassword == null || !encoder.matches(currentPassword, u.getPasswordHash()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current password invalid");
            u.setPasswordHash(encoder.encode(newPassword));
        }
        return users.save(u);
    }
}