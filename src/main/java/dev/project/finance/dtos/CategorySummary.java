package dev.project.finance.dtos;

import dev.project.finance.models.CategoryType;

import java.time.LocalDateTime;

public record CategorySummary(
        Long id,
        String nome,
        CategoryType tipo,
        LocalDateTime criadoEm
) {}