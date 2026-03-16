package dev.project.finance.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "O nome da conta é obrigatório")
        @Size(max = 100, message = "O nome da conta deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O tipo da conta é obrigatório")
        @Size(max = 30, message = "O tipo da conta deve ter no máximo 30 caracteres")
        String tipo,

        @NotNull(message = "O saldo inicial é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "O saldo inicial não pode ser negativo")
        BigDecimal saldoInicial,

        @NotNull(message = "Informar se está ativo é obrigatório")
        Boolean ativo
) {}
