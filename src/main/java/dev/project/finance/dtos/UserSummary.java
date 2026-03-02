package dev.project.finance.dtos;

import dev.project.finance.models.Roles;

public record UserSummary(
        Long id,
        String email,
        String role
) {}