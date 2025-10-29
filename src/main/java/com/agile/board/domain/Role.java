package com.agile.board.domain;


import com.agile.board.config.Permissions;

import java.util.List;

public enum Role {
    USER,
    ADMIN;

    public List<String> permissions() {
        return switch (this) {
            case ADMIN -> List.of(
                    Permissions.PROJECT_VIEW,
                    Permissions.PROJECT_CREATE,
                    Permissions.PROJECT_EDIT
            );
            case USER -> List.of(
                    Permissions.PROJECT_VIEW
            );
        };
    }

    public static Role from(String value) {
        if (value == null) return USER;
        try { return Role.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException ex) { return USER; }
    }
}
