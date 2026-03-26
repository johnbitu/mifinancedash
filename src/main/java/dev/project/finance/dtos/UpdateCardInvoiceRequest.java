package dev.project.finance.dtos;

import dev.project.finance.models.InvoiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCardInvoiceRequest(
        @Min(value = 1, message = "Mes de referencia deve estar entre 1 e 12")
        @Max(value = 12, message = "Mes de referencia deve estar entre 1 e 12")
        Integer mesReferencia,

        @Min(value = 2000, message = "Ano de referencia invalido")
        @Max(value = 2100, message = "Ano de referencia invalido")
        Integer anoReferencia,

        @DecimalMin(value = "0.0", inclusive = true, message = "Valor total nao pode ser negativo")
        BigDecimal valorTotal,

        LocalDate dataVencimento,

        InvoiceStatus status
) {
}
