package dev.project.finance.dtos;

import dev.project.finance.models.RecurrenceFrequency;
import dev.project.finance.models.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRecurrenceRequest(
        @NotBlank(message = "O nome da recorrencia e obrigatorio")
        @Size(max = 100, message = "O nome da recorrencia deve ter no maximo 100 caracteres")
        String nome,
        String descricao,
        @NotNull(message = "O tipo e obrigatorio")
        TransactionType tipo,
        @NotNull(message = "O valor e obrigatorio")
        BigDecimal valor,
        @NotNull(message = "A frequencia e obrigatoria")
        RecurrenceFrequency frequencia,
        Integer diaCobranca,
        @NotNull(message = "A data de inicio e obrigatoria")
        LocalDate dataInicio,
        LocalDate dataFim,
        @NotNull(message = "A conta e obrigatoria")
        Long accountId,
        Long categoryId,
        Long cardId
) {
}
