package dev.project.finance.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull(message = "A conta é obrigatória")
        Long accountId,

        Long categoryId,

        @NotBlank(message = "O tipo da transação é obrigatório")
        @Size(max = 20, message = "O tipo da transação deve ter no máximo 20 caracteres")
        String tipo,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", inclusive = true, message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "A data da transação é obrigatória")
        LocalDate dataTransacao,

        String observacao
) {}
