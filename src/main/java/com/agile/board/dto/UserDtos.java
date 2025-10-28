package com.agile.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserDtos {
    public record UserView(Long id, String username, String email, String name, String role) {
    }

    public record UpdateProfileRequest(
            @Size(min = 1, max = 100)
            String name,
            @Email
            String email,
            @Size(min = 6, max = 100)
            String newPassword,
            String currentPassword
    ) {
    }
}
