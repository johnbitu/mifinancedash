package dev.project.finance.dtos;

import dev.project.finance.models.Roles;

public record UserLogin(
        Long id,
        String email,
        Roles role
) {}
