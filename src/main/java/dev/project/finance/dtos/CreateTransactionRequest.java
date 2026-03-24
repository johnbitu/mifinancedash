package dev.project.finance.dtos;

import dev.project.finance.models.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull(message = "A conta e obrigatoria")
        Long accountId,

        Long categoryId,

        Long cardId,

        @NotNull(message = "O tipo da transacao e obrigatorio")
        TransactionType tipo,

        @NotNull(message = "O valor e obrigatorio")
        @DecimalMin(value = "0.01", inclusive = true, message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotBlank(message = "A descricao e obrigatoria")
        @Size(max = 255, message = "A descricao deve ter no maximo 255 caracteres")
        String descricao,

        @NotNull(message = "A data da transacao e obrigatoria")
        LocalDate dataTransacao,

        String observacao
) {
}
