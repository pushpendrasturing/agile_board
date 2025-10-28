package com.agile.board.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {
    public record RegisterRequest(@NotBlank String username,
                                  @NotBlank @Email String email,
                                  @NotBlank String password) {}

    public record LoginRequest(@NotBlank String username,
                               @NotBlank String password) {}

    public record JwtResponse(String token) {}
}
