package dev.project.finance.services;

import dev.project.finance.dtos.DepositarGoalRequest;
import dev.project.finance.dtos.GoalSummary;
import dev.project.finance.models.Account;
import dev.project.finance.models.Goal;
import dev.project.finance.models.GoalStatus;
import dev.project.finance.models.Transaction;
import dev.project.finance.models.TransactionType;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.GoalRepository;
import dev.project.finance.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private GoalService goalService;

    @Test
    void depositarCriaTransacaoComValorEfetivoEAjustaSaldoDaConta() {
        Long userId = 1L;
        Long goalId = 10L;
        Long accountId = 20L;

        User user = new User();
        user.setId(userId);

        Account account = Account.builder()
                .nome("Conta Principal")
                .saldoInicial(new BigDecimal("1000.00"))
                .saldoAtual(new BigDecimal("1000.00"))
                .ativo(true)
                .user(user)
                .build();
        account.setId(accountId);

        Goal goal = Goal.builder()
                .nome("Reserva de Emergencia")
                .valorAlvo(new BigDecimal("150.00"))
                .valorAtual(new BigDecimal("100.00"))
                .dataInicio(LocalDate.now().minusDays(5))
                .dataPrazo(LocalDate.now().plusDays(30))
                .status(GoalStatus.EM_ANDAMENTO)
                .account(account)
                .user(user)
                .build();
        goal.setId(goalId);

        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.findByIdAndUserIdAndAtivoTrue(accountId, userId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(99L);
            return transaction;
        });
        when(transactionRepository.somatorioPorConta(accountId)).thenReturn(new BigDecimal("-250.00"));

        GoalSummary summary = goalService.depositar(goalId, userId, new DepositarGoalRequest(new BigDecimal("80.00")));

        assertEquals(new BigDecimal("150.00"), summary.valorAtual());
        assertEquals(GoalStatus.CONCLUIDA, summary.status());
        assertEquals(new BigDecimal("750.00"), account.getSaldoAtual());

        ArgumentCaptor<Transaction> transacaoCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transacaoCaptor.capture());
        Transaction transacaoCriada = transacaoCaptor.getValue();
        assertEquals(TransactionType.DESPESA, transacaoCriada.getTipo());
        assertEquals(new BigDecimal("50.00"), transacaoCriada.getValor());
        assertEquals("Aporte na meta: Reserva de Emergencia", transacaoCriada.getDescricao());
        assertEquals(accountId, transacaoCriada.getAccount().getId());
        assertEquals(userId, transacaoCriada.getUser().getId());

        verify(auditService).registrar(eq("GOAL_DEPOSITO"), eq(userId), eq("N/A"), eq(true), contains("transacao 99"));
    }

    @Test
    void depositarFalhaQuandoMetaNaoTemContaVinculada() {
        Long userId = 1L;
        Long goalId = 10L;

        User user = new User();
        user.setId(userId);

        Goal goal = Goal.builder()
                .nome("Meta sem conta")
                .valorAlvo(new BigDecimal("300.00"))
                .valorAtual(new BigDecimal("100.00"))
                .dataInicio(LocalDate.now().minusDays(5))
                .dataPrazo(LocalDate.now().plusDays(30))
                .status(GoalStatus.EM_ANDAMENTO)
                .account(null)
                .user(user)
                .build();
        goal.setId(goalId);

        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalArgumentException.class,
                () -> goalService.depositar(goalId, userId, new DepositarGoalRequest(new BigDecimal("50.00"))));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
