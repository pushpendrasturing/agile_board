package com.agile.board.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class CommentDtos {
    public record CreateComment(@NotBlank String text) {
    }

    public record CommentView(Long id, String text, String createdBy, Instant createdAt) {
    }
}
