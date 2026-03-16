package dev.project.finance.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountSummary(
        Long id,
        String nome,
        String tipo,
        BigDecimal saldoInicial,
        Boolean ativo,
        LocalDateTime criadoEm
) {}