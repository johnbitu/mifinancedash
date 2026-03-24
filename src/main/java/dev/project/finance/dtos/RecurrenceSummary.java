package dev.project.finance.dtos;

import dev.project.finance.models.RecurrenceFrequency;
import dev.project.finance.models.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecurrenceSummary(
        Long id,
        String nome,
        String descricao,
        TransactionType tipo,
        BigDecimal valor,
        RecurrenceFrequency frequencia,
        Integer diaCobranca,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate proximaExecucao,
        Boolean ativo,
        Long accountId,
        String accountNome,
        Long categoryId,
        String categoryNome,
        Long cardId,
        String cardNome,
        LocalDateTime ultimaFalha,
        String motivoFalha,
        LocalDateTime criadoEm
) {
}
