package com.agile.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetDtos {
    public record RequestReset(@NotBlank @Email String emailOrUsername) {}
    public record ResetPassword(@NotBlank String token, @NotBlank @Size(min = 6, max = 100) String newPassword) {}
    public record ResetTokenResponse(String resetToken) {}
    public record GenericMessage(String message) {}
}