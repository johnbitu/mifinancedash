package dev.project.finance.dtos;

import dev.project.finance.models.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountSummary(
        Long id,
        String nome,
        AccountType tipo,
        BigDecimal saldoInicial,
        BigDecimal saldoAtual,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
