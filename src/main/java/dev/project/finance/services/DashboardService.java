package dev.project.finance.services;

import dev.project.finance.dtos.DashboardResponse;
import dev.project.finance.dtos.FaturaResumida;
import dev.project.finance.dtos.GoalResumida;
import dev.project.finance.dtos.RecurrenceResumida;
import dev.project.finance.models.GoalStatus;
import dev.project.finance.models.InvoiceStatus;
import dev.project.finance.models.TransactionType;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.CardInvoiceRepository;
import dev.project.finance.repositories.CardRepository;
import dev.project.finance.repositories.GoalRepository;
import dev.project.finance.repositories.RecurrenceRepository;
import dev.project.finance.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CardInvoiceRepository cardInvoiceRepository;
    private final GoalRepository goalRepository;
    private final RecurrenceRepository recurrenceRepository;

    @Transactional(readOnly = true)
    public DashboardResponse resumo(Long userId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate fimMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        BigDecimal saldoTotal = accountRepository.somarSaldoAtualAtivoPorUsuario(userId);
        BigDecimal totalReceitas = transactionRepository.sumValorByUserIdAndTipoAndDataTransacaoBetween(
                userId,
                TransactionType.RECEITA,
                inicioMes,
                fimMes
        );
        BigDecimal totalDespesas = transactionRepository.sumValorByUserIdAndTipoAndDataTransacaoBetween(
                userId,
                TransactionType.DESPESA,
                inicioMes,
                fimMes
        );

        BigDecimal limiteCartaoTotal = cardRepository.findByUserIdAndAtivoTrue(userId).stream()
                .filter(card -> card.getLimite() != null)
                .map(card -> card.getLimite())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limiteCartaoUsado = cardRepository.findByUserIdAndAtivoTrue(userId).stream()
                .filter(card -> card.getLimite() != null && card.getLimiteDisponivel() != null)
                .map(card -> card.getLimite().subtract(card.getLimiteDisponivel()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FaturaResumida> faturas = cardInvoiceRepository
                .findByUserIdAndStatusIn(userId, List.of(InvoiceStatus.ABERTA, InvoiceStatus.FECHADA))
                .stream()
                .map(invoice -> new FaturaResumida(
                        invoice.getId(),
                        invoice.getCard().getNome(),
                        invoice.getMesReferencia(),
                        invoice.getAnoReferencia(),
                        invoice.getValorTotal(),
                        invoice.getStatus().name()
                ))
                .toList();

        List<GoalResumida> metas = goalRepository.findByUserIdAndStatus(userId, GoalStatus.EM_ANDAMENTO)
                .stream()
                .map(goal -> {
                    BigDecimal percentual = goal.getValorAlvo().compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : goal.getValorAtual()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(goal.getValorAlvo(), 2, RoundingMode.HALF_UP);
                    return new GoalResumida(goal.getId(), goal.getNome(), percentual);
                })
                .toList();

        List<RecurrenceResumida> recorrencias = recurrenceRepository
                .findTop5ByUserIdAndAtivoTrueOrderByProximaExecucaoAsc(userId)
                .stream()
                .map(recurrence -> new RecurrenceResumida(
                        recurrence.getId(),
                        recurrence.getNome(),
                        recurrence.getProximaExecucao(),
                        recurrence.getFrequencia().name()
                ))
                .toList();

        return new DashboardResponse(
                saldoTotal,
                totalReceitas,
                totalDespesas,
                totalReceitas.subtract(totalDespesas),
                limiteCartaoTotal,
                limiteCartaoUsado,
                faturas,
                metas,
                recorrencias
        );
    }
}
