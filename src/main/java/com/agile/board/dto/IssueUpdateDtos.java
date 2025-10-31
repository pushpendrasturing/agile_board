package com.agile.board.dto;

import com.agile.board.domain.IssueStatus;
import jakarta.validation.constraints.NotNull;

public class IssueUpdateDtos {
    public record UpdateStatus(@NotNull IssueStatus status) {}
}
