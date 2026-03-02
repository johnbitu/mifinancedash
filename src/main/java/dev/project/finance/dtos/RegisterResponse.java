package dev.project.finance.dtos;

import dev.project.finance.models.Roles;

import java.time.LocalDateTime;

// RegisterResponse.java — Record limpo, sem expor entidade
public record RegisterResponse(
        Long id,
        String nome,
        String email,
        Roles role,
        LocalDateTime createdAt
) {}