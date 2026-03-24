package dev.project.finance.dtos;

import dev.project.finance.models.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CardInvoiceSummary(
        Long id,
        Long cardId,
        String cardNome,
        Integer mesReferencia,
        Integer anoReferencia,
        BigDecimal valorTotal,
        LocalDate dataVencimento,
        InvoiceStatus status,
        LocalDateTime criadoEm
) {
}
