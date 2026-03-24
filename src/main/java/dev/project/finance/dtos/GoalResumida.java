package dev.project.finance.dtos;

import java.math.BigDecimal;

public record GoalResumida(
        Long id,
        String nome,
        BigDecimal percentualConcluido
) {
}
