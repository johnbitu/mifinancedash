package dev.project.finance.dtos;

import java.math.BigDecimal;

public record FaturaResumida(
        Long id,
        String cartao,
        Integer mes,
        Integer ano,
        BigDecimal valor,
        String status
) {
}
