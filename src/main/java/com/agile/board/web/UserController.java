package com.agile.board.web;

import com.agile.board.domain.User;
import com.agile.board.dto.UserDtos;
import com.agile.board.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/me")
    public ResponseEntity<UserDtos.UserView> updateMe(Authentication auth, @Valid @RequestBody UserDtos.UpdateProfileRequest req) {
        String username = auth.getName();
        User u = userService.updateProfile(username, req.name(), req.email(), req.newPassword(), req.currentPassword());
        return ResponseEntity.ok(new UserDtos.UserView(u.getId(), u.getUsername(), u.getEmail(), u.getName(), u.getRole()));
    }
}
