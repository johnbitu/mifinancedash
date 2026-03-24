package dev.project.finance.services;

import dev.project.finance.dtos.CreateRecurrenceRequest;
import dev.project.finance.dtos.RecurrenceHistoricoResponse;
import dev.project.finance.dtos.RecurrenceSummary;
import dev.project.finance.dtos.TransactionSummary;
import dev.project.finance.exceptions.AccountNotFoundException;
import dev.project.finance.exceptions.CategoryNotFoundException;
import dev.project.finance.exceptions.RecurrenceNotFoundException;
import dev.project.finance.models.Account;
import dev.project.finance.models.Card;
import dev.project.finance.models.Category;
import dev.project.finance.models.Recurrence;
import dev.project.finance.models.RecurrenceFrequency;
import dev.project.finance.models.Transaction;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.CardRepository;
import dev.project.finance.repositories.CategoryRepository;
import dev.project.finance.repositories.RecurrenceRepository;
import dev.project.finance.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurrenceService {

    private final RecurrenceRepository recurrenceRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final CardService cardService;
    private final AuditService auditService;

    @Transactional
    public RecurrenceSummary create(CreateRecurrenceRequest request, User user) {
        validarDatas(request.dataInicio(), request.dataFim());

        Recurrence recurrence = Recurrence.builder()
                .user(user)
                .account(buscarConta(request.accountId(), user.getId()))
                .category(buscarCategoriaOpcional(request.categoryId(), user.getId()))
                .card(buscarCardOpcional(request.cardId(), user.getId()))
                .nome(request.nome())
                .descricao(request.descricao())
                .tipo(request.tipo())
                .valor(request.valor())
                .frequencia(request.frequencia())
                .diaCobranca(request.diaCobranca())
                .dataInicio(request.dataInicio())
                .dataFim(request.dataFim())
                .proximaExecucao(request.dataInicio())
                .ativo(true)
                .build();

        return toSummary(recurrenceRepository.save(recurrence));
    }

    @Transactional(readOnly = true)
    public List<RecurrenceSummary> listar(Long userId, Boolean ativo) {
        List<Recurrence> recurrences = ativo == null
                ? recurrenceRepository.findByUserIdOrderByProximaExecucaoAsc(userId)
                : recurrenceRepository.findByUserIdAndAtivo(userId, ativo);
        return recurrences.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public RecurrenceSummary buscar(Long id, Long userId) {
        return toSummary(buscarRecurrence(id, userId));
    }

    @Transactional
    public RecurrenceSummary atualizar(Long id, Long userId, CreateRecurrenceRequest request) {
        Recurrence recurrence = buscarRecurrence(id, userId);
        validarDatas(request.dataInicio(), request.dataFim());

        recurrence.setNome(request.nome());
        recurrence.setDescricao(request.descricao());
        recurrence.setTipo(request.tipo());
        recurrence.setValor(request.valor());
        recurrence.setFrequencia(request.frequencia());
        recurrence.setDiaCobranca(request.diaCobranca());
        recurrence.setDataInicio(request.dataInicio());
        recurrence.setDataFim(request.dataFim());
        recurrence.setAccount(buscarConta(request.accountId(), userId));
        recurrence.setCategory(buscarCategoriaOpcional(request.categoryId(), userId));
        recurrence.setCard(buscarCardOpcional(request.cardId(), userId));

        if (recurrence.getProximaExecucao().isBefore(LocalDate.now())) {
            recurrence.setProximaExecucao(LocalDate.now());
        }

        return toSummary(recurrenceRepository.save(recurrence));
    }

    @Transactional
    public void desativar(Long id, Long userId) {
        Recurrence recurrence = buscarRecurrence(id, userId);
        recurrence.setAtivo(false);
        recurrenceRepository.save(recurrence);
    }

    @Transactional
    public void executarManual(Long id, Long userId) {
        Recurrence recurrence = buscarRecurrence(id, userId);
        executar(recurrence);
    }

    @Transactional
    public ExecutionResult executar(Recurrence recurrence) {
        if (!Boolean.TRUE.equals(recurrence.getAtivo())) {
            return ExecutionResult.skipped();
        }

        try {
            Transaction transaction = Transaction.builder()
                    .user(recurrence.getUser())
                    .account(recurrence.getAccount())
                    .category(recurrence.getCategory())
                    .card(recurrence.getCard())
                    .recurrence(recurrence)
                    .tipo(recurrence.getTipo())
                    .valor(recurrence.getValor())
                    .descricao(recurrence.getNome())
                    .dataTransacao(LocalDate.now())
                    .observacao("Gerado automaticamente por recorrencia")
                    .build();

            transactionRepository.save(transaction);
            if (transaction.getCard() != null) {
                cardService.processarTransacao(transaction);
            }
            recalcularSaldo(transaction.getAccount());

            recurrence.setMotivoFalha(null);
            recurrence.setUltimaFalha(null);

            LocalDate proxima = calcularProximaExecucao(recurrence.getProximaExecucao(), recurrence.getFrequencia());
            recurrence.setProximaExecucao(proxima);
            if (recurrence.getDataFim() != null && proxima.isAfter(recurrence.getDataFim())) {
                recurrence.setAtivo(false);
            }

            recurrenceRepository.save(recurrence);
            auditService.registrar("RECORRENCIA_EXECUTADA", recurrence.getUser().getId(), "N/A", true,
                    "Recorrencia " + recurrence.getId() + " executada com sucesso");
            return ExecutionResult.executed();
        } catch (Exception ex) {
            recurrence.setUltimaFalha(LocalDateTime.now());
            recurrence.setMotivoFalha(ex.getMessage());
            recurrenceRepository.save(recurrence);
            auditService.registrar("RECORRENCIA_FALHA", recurrence.getUser().getId(), "N/A", false,
                    "Recorrencia " + recurrence.getId() + " falhou: " + ex.getMessage());
            return ExecutionResult.failed();
        }
    }

    @Transactional(readOnly = true)
    public List<Recurrence> listarPendentesExecucao(LocalDate hoje) {
        return recurrenceRepository.findByAtivoTrueAndProximaExecucaoLessThanEqual(hoje);
    }

    @Transactional(readOnly = true)
    public RecurrenceHistoricoResponse historico(Long id, Long userId, TransactionService transactionService) {
        Recurrence recurrence = buscarRecurrence(id, userId);
        List<TransactionSummary> transacoes = transactionRepository.findByRecurrenceId(recurrence.getId())
                .stream()
                .map(transactionService::toSummary)
                .toList();
        return new RecurrenceHistoricoResponse(transacoes);
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataFim != null && !dataFim.isAfter(dataInicio)) {
            throw new IllegalArgumentException("A data fim deve ser posterior a data inicio");
        }
    }

    public LocalDate calcularProximaExecucao(LocalDate atual, RecurrenceFrequency frequencia) {
        return switch (frequencia) {
            case DIARIA -> atual.plusDays(1);
            case SEMANAL -> atual.plusWeeks(1);
            case QUINZENAL -> atual.plusWeeks(2);
            case MENSAL -> atual.plusMonths(1);
            case BIMESTRAL -> atual.plusMonths(2);
            case TRIMESTRAL -> atual.plusMonths(3);
            case SEMESTRAL -> atual.plusMonths(6);
            case ANUAL -> atual.plusYears(1);
        };
    }

    private Recurrence buscarRecurrence(Long id, Long userId) {
        return recurrenceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RecurrenceNotFoundException("Recorrencia nao encontrada"));
    }

    private Account buscarConta(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserIdAndAtivoTrue(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta nao encontrada"));
    }

    private Category buscarCategoriaOpcional(Long categoryId, Long userId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria nao encontrada"));
    }

    private Card buscarCardOpcional(Long cardId, Long userId) {
        if (cardId == null) {
            return null;
        }

        return cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new RecurrenceNotFoundException("Cartao da recorrencia nao encontrado"));
    }

    private void recalcularSaldo(Account account) {
        BigDecimal somatorio = transactionRepository.somatorioPorConta(account.getId());
        account.setSaldoAtual(account.getSaldoInicial().add(somatorio));
        accountRepository.save(account);
    }

    public RecurrenceSummary toSummary(Recurrence recurrence) {
        return new RecurrenceSummary(
                recurrence.getId(),
                recurrence.getNome(),
                recurrence.getDescricao(),
                recurrence.getTipo(),
                recurrence.getValor(),
                recurrence.getFrequencia(),
                recurrence.getDiaCobranca(),
                recurrence.getDataInicio(),
                recurrence.getDataFim(),
                recurrence.getProximaExecucao(),
                recurrence.getAtivo(),
                recurrence.getAccount().getId(),
                recurrence.getAccount().getNome(),
                recurrence.getCategory() != null ? recurrence.getCategory().getId() : null,
                recurrence.getCategory() != null ? recurrence.getCategory().getNome() : null,
                recurrence.getCard() != null ? recurrence.getCard().getId() : null,
                recurrence.getCard() != null ? recurrence.getCard().getNome() : null,
                recurrence.getUltimaFalha(),
                recurrence.getMotivoFalha(),
                recurrence.getCriadoEm()
        );
    }

    public record ExecutionResult(boolean executada, boolean falhou) {
        static ExecutionResult executed() {
            return new ExecutionResult(true, false);
        }

        static ExecutionResult failed() {
            return new ExecutionResult(false, true);
        }

        static ExecutionResult skipped() {
            return new ExecutionResult(false, false);
        }
    }
}
