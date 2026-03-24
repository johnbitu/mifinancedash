package dev.project.finance.dtos;

import dev.project.finance.models.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @jakarta.validation.constraints.NotBlank(message = "O nome da conta e obrigatorio")
        @jakarta.validation.constraints.Size(max = 100, message = "O nome da conta deve ter no maximo 100 caracteres")
        String nome,

        @NotNull(message = "O tipo da conta e obrigatorio")
        AccountType tipo,

        @NotNull(message = "O saldo inicial e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "O saldo inicial nao pode ser negativo")
        @PositiveOrZero(message = "O saldo inicial nao pode ser negativo")
        BigDecimal saldoInicial
) {
}
