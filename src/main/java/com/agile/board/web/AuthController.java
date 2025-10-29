package com.agile.board.web;

import com.agile.board.dto.AuthDtos.*;
import com.agile.board.dto.PasswordResetDtos;
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

    @PostMapping("/request-reset")
    public ResponseEntity<PasswordResetDtos.ResetTokenResponse> requestReset(@Valid @RequestBody PasswordResetDtos.RequestReset req) {
        String token = auth.requestPasswordResetByEmailOrUsername(req.emailOrUsername());
        return ResponseEntity.ok(new PasswordResetDtos.ResetTokenResponse(token));
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordResetDtos.GenericMessage> reset(@Valid @RequestBody PasswordResetDtos.ResetPassword req) {
        auth.resetPasswordWithToken(req.token(), req.newPassword());
        return ResponseEntity.ok(new PasswordResetDtos.GenericMessage("Password reset successful"));
    }
}
