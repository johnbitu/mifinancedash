package dev.project.finance.dtos;

import java.time.LocalDateTime;

// RegisterResponse.java — Record limpo, sem expor entidade
public record RegisterResponse(
        Long id,
        String nome,
        String email,
        LocalDateTime createdAt
) {}