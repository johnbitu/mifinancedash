package dev.project.finance.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountSummaryComSaldo(
        Long id,
        String nome,
        String tipo,
        BigDecimal saldoInicial,  // valor fixo cadastrado
        BigDecimal saldoAtual,    // calculado: saldoInicial + receitas - despesas
        Boolean ativo,
        LocalDateTime criadoEm
) {}