package dev.project.finance.dtos;

import dev.project.finance.models.Roles;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String nome,
        String email,
        Roles role,
        LocalDateTime createdAt
) {
}
