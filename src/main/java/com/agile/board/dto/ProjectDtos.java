package com.agile.board.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public class ProjectDtos {
    public record ProjectCreate(@NotBlank String key, @NotBlank String name) {}
    public record ProjectView(Long id, String key, String name, Set<Long> memberIds) {}
}
