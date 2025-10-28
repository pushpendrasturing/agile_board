package com.agile.board.web;

import com.agile.board.dto.AuthDtos.*;
import com.agile.board.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        auth.register(req.username(), req.email(), req.password());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest req) {
        String token = auth.login(req.username(), req.password());
        return new JwtResponse(token);
    }
}
