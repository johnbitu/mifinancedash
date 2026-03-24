package dev.project.finance.dtos;

import dev.project.finance.models.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalSummary(
        Long id,
        String nome,
        String descricao,
        BigDecimal valorAlvo,
        BigDecimal valorAtual,
        LocalDate dataInicio,
        LocalDate dataPrazo,
        GoalStatus status,
        Long accountId,
        String accountNome,
        LocalDateTime criadoEm
) {
}
