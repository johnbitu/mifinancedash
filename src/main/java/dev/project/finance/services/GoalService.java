package dev.project.finance.services;

import dev.project.finance.dtos.CreateGoalRequest;
import dev.project.finance.dtos.DepositarGoalRequest;
import dev.project.finance.dtos.GoalProgressoResponse;
import dev.project.finance.dtos.GoalSummary;
import dev.project.finance.exceptions.AccountNotFoundException;
import dev.project.finance.exceptions.GoalConcluidaException;
import dev.project.finance.exceptions.GoalNaoEditavelException;
import dev.project.finance.exceptions.GoalNotFoundException;
import dev.project.finance.models.Account;
import dev.project.finance.models.Goal;
import dev.project.finance.models.GoalStatus;
import dev.project.finance.models.Transaction;
import dev.project.finance.models.TransactionType;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.GoalRepository;
import dev.project.finance.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    @Transactional
    public GoalSummary create(CreateGoalRequest request, User user) {
        validarRegrasDatas(request.dataInicio(), request.dataPrazo());
        validarValorAlvo(request.valorAlvo());

        Goal goal = Goal.builder()
                .user(user)
                .account(buscarContaObrigatoria(request.accountId(), user.getId()))
                .nome(request.nome())
                .descricao(request.descricao())
                .valorAlvo(request.valorAlvo())
                .valorAtual(BigDecimal.ZERO)
                .dataInicio(request.dataInicio())
                .dataPrazo(request.dataPrazo())
                .status(GoalStatus.EM_ANDAMENTO)
                .build();

        return toSummary(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalSummary> listar(Long userId, GoalStatus status) {
        List<Goal> goals = status == null
                ? goalRepository.findByUserIdOrderByDataPrazoAsc(userId)
                : goalRepository.findByUserIdAndStatus(userId, status);
        return goals.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public GoalSummary buscar(Long id, Long userId) {
        return toSummary(buscarGoal(id, userId));
    }

    @Transactional
    public GoalSummary atualizar(Long id, Long userId, CreateGoalRequest request) {
        Goal goal = buscarGoal(id, userId);
        validarEditavel(goal);
        validarRegrasDatas(request.dataInicio(), request.dataPrazo());
        validarValorAlvo(request.valorAlvo());

        goal.setNome(request.nome());
        goal.setDescricao(request.descricao());
        goal.setValorAlvo(request.valorAlvo());
        goal.setDataInicio(request.dataInicio());
        goal.setDataPrazo(request.dataPrazo());
        goal.setAccount(buscarContaOpcional(request.accountId(), userId));

        if (goal.getValorAtual().compareTo(goal.getValorAlvo()) >= 0) {
            goal.setStatus(GoalStatus.CONCLUIDA);
        }

        return toSummary(goalRepository.save(goal));
    }

    @Transactional
    public void deletar(Long id, Long userId) {
        Goal goal = buscarGoal(id, userId);
        if (goal.getStatus() == GoalStatus.CONCLUIDA) {
            throw new GoalConcluidaException("Metas concluidas nao podem ser removidas");
        }

        if (goal.getStatus() != GoalStatus.CANCELADA && goal.getStatus() != GoalStatus.EM_ANDAMENTO) {
            throw new GoalNaoEditavelException("Somente metas EM_ANDAMENTO ou CANCELADA podem ser removidas");
        }

        goalRepository.delete(goal);
    }

    @Transactional
    public GoalSummary depositar(Long id, Long userId, DepositarGoalRequest request) {
        Goal goal = buscarGoal(id, userId);
        validarEditavel(goal);

        BigDecimal valorAnterior = goal.getValorAtual();
        BigDecimal novoValor = valorAnterior.add(request.valor());
        if (novoValor.compareTo(goal.getValorAlvo()) > 0) {
            novoValor = goal.getValorAlvo();
        }
        BigDecimal valorDepositado = novoValor.subtract(valorAnterior);

        goal.setValorAtual(novoValor);
        if (goal.getValorAtual().compareTo(goal.getValorAlvo()) >= 0) {
            goal.setStatus(GoalStatus.CONCLUIDA);
        }

        Goal atualizado = goalRepository.save(goal);
        Transaction transacao = criarTransacaoDepositoMeta(atualizado, userId, valorDepositado);
        auditService.registrar("GOAL_DEPOSITO", userId, "N/A", true,
                "Meta " + atualizado.getId() + " recebeu deposito de " + valorDepositado
                        + " e gerou transacao " + transacao.getId());

        return toSummary(atualizado);
    }

    @Transactional(readOnly = true)
    public GoalProgressoResponse progresso(Long id, Long userId) {
        Goal goal = buscarGoal(id, userId);
        BigDecimal percentual = goal.getValorAlvo().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : goal.getValorAtual()
                .multiply(BigDecimal.valueOf(100))
                .divide(goal.getValorAlvo(), 2, RoundingMode.HALF_UP);

        BigDecimal restante = goal.getValorAlvo().subtract(goal.getValorAtual()).max(BigDecimal.ZERO);
        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDataPrazo());

        return new GoalProgressoResponse(
                goal.getValorAlvo(),
                goal.getValorAtual(),
                percentual,
                restante,
                diasRestantes,
                goal.getStatus()
        );
    }

    @Transactional
    public int expirarMetasVencidas() {
        List<Goal> metas = goalRepository.findByStatusAndDataPrazoLessThan(GoalStatus.EM_ANDAMENTO, LocalDate.now());
        metas.forEach(meta -> meta.setStatus(GoalStatus.EXPIRADA));
        goalRepository.saveAll(metas);
        return metas.size();
    }

    private Goal buscarGoal(Long id, Long userId) {
        return goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new GoalNotFoundException("Meta nao encontrada"));
    }

    private void validarEditavel(Goal goal) {
        if (goal.getStatus() == GoalStatus.CONCLUIDA || goal.getStatus() == GoalStatus.CANCELADA) {
            throw new GoalNaoEditavelException("Meta concluida ou cancelada nao pode ser alterada");
        }
    }

    private void validarRegrasDatas(LocalDate dataInicio, LocalDate dataPrazo) {
        if (!dataPrazo.isAfter(dataInicio)) {
            throw new IllegalArgumentException("A data prazo deve ser posterior a data de inicio");
        }
    }

    private void validarValorAlvo(BigDecimal valorAlvo) {
        if (valorAlvo == null || valorAlvo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor alvo deve ser maior que zero");
        }
    }

    private Account buscarContaOpcional(Long accountId, Long userId) {
        if (accountId == null) {
            return null;
        }

        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta nao encontrada"));
    }

    private Account buscarContaObrigatoria(Long accountId, Long userId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Conta vinculada e obrigatoria na criacao da meta");
        }

        return buscarContaOpcional(accountId, userId);
    }

    private Transaction criarTransacaoDepositoMeta(Goal goal, Long userId, BigDecimal valorDepositado) {
        if (goal.getAccount() == null) {
            throw new IllegalArgumentException(
                    "A meta precisa ter conta vinculada para registrar o deposito como transacao");
        }

        Account account = accountRepository.findByIdAndUserIdAndAtivoTrue(goal.getAccount().getId(), userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta vinculada a meta nao encontrada ou inativa"));

        Transaction transaction = Transaction.builder()
                .tipo(TransactionType.DESPESA)
                .valor(valorDepositado)
                .descricao("Aporte na meta: " + goal.getNome())
                .dataTransacao(LocalDate.now())
                .observacao("Deposito na meta id=" + goal.getId())
                .user(goal.getUser())
                .account(account)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        recalcularSaldo(account);
        accountRepository.save(account);
        return saved;
    }

    private void recalcularSaldo(Account account) {
        BigDecimal somatorio = transactionRepository.somatorioPorConta(account.getId());
        account.setSaldoAtual(account.getSaldoInicial().add(somatorio));
    }

    public GoalSummary toSummary(Goal goal) {
        return new GoalSummary(
                goal.getId(),
                goal.getNome(),
                goal.getDescricao(),
                goal.getValorAlvo(),
                goal.getValorAtual(),
                goal.getDataInicio(),
                goal.getDataPrazo(),
                goal.getStatus(),
                goal.getAccount() != null ? goal.getAccount().getId() : null,
                goal.getAccount() != null ? goal.getAccount().getNome() : null,
                goal.getCriadoEm()
        );
    }
}
