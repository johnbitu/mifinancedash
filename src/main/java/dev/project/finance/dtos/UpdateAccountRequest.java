package dev.project.finance.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateAccountRequest(
        @NotBlank(message = "O nome da conta e obrigatorio")
        @Size(max = 100, message = "O nome da conta deve ter no maximo 100 caracteres")
        String nome,

        @NotBlank(message = "O tipo da conta e obrigatorio")
        @Size(max = 30, message = "O tipo da conta deve ter no maximo 30 caracteres")
        String tipo,

        @NotNull(message = "O saldo inicial e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "O saldo inicial nao pode ser negativo")
        BigDecimal saldoInicial,

        @NotNull
        Boolean ativo
) {
}
