package dev.project.finance.dtos;

import dev.project.finance.models.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionSummary(
        Long id,
        TransactionType tipo,
        BigDecimal valor,
        String descricao,
        LocalDate dataTransacao,
        String observacao,
        LocalDateTime criadoEm,
        Long accountId,
        String accountNome,
        Long categoryId,
        String categoryNome,
        Long cardId,
        String cardNome,
        Long recurrenceId
) {
}
