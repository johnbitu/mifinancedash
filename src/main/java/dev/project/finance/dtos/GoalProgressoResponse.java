package dev.project.finance.dtos;

import dev.project.finance.models.GoalStatus;

import java.math.BigDecimal;

public record GoalProgressoResponse(
        BigDecimal valorAlvo,
        BigDecimal valorAtual,
        BigDecimal percentualConcluido,
        BigDecimal valorRestante,
        long diasRestantes,
        GoalStatus status
) {
}
