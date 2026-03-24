package dev.project.finance.dtos;

import dev.project.finance.models.CardBandeira;
import dev.project.finance.models.CardType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardSummary(
        Long id,
        String nome,
        CardBandeira bandeira,
        CardType tipo,
        Long accountId,
        String accountNome,
        BigDecimal limite,
        BigDecimal limiteDisponivel,
        Integer diaFechamento,
        Integer diaVencimento,
        Boolean ativo,
        LocalDateTime criadoEm
) {
}
