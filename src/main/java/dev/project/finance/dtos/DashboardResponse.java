package dev.project.finance.dtos;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal saldoTotal,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldoMes,
        BigDecimal limiteCartaoTotal,
        BigDecimal limiteCartaoUsado,
        List<FaturaResumida> faturas,
        List<GoalResumida> metas,
        List<RecurrenceResumida> recorrencias
) {
}
