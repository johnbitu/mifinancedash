package dev.project.finance.dtos;

import dev.project.finance.models.CardBandeira;
import dev.project.finance.models.CardType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCardRequest(
        @NotBlank(message = "O nome do cartao e obrigatorio")
        @Size(max = 100, message = "O nome do cartao deve ter no maximo 100 caracteres")
        String nome,

        @NotNull(message = "A bandeira do cartao e obrigatoria")
        CardBandeira bandeira,

        @NotNull(message = "O tipo do cartao e obrigatorio")
        CardType tipo,

        Long accountId,

        @DecimalMin(value = "0.0", inclusive = true, message = "O limite nao pode ser negativo")
        BigDecimal limite,

        Integer diaFechamento,

        Integer diaVencimento
) {
}
