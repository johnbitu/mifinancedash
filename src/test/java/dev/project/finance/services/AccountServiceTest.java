package dev.project.finance.services;

import dev.project.finance.dtos.AccountSummaryComSaldo;
import dev.project.finance.models.Account;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void retornaSaldoCalculadoQuandoRepositorioTemResultado() {
        Long accountId = 10L;
        Long userId = 20L;
        BigDecimal saldoInicial = new BigDecimal("100.00");
        BigDecimal saldoCalculado = new BigDecimal("175.50");

        Account account = Account.builder()
                .id(accountId)
                .nome("Conta Principal")
                .tipo("CARTEIRA")
                .saldoInicial(saldoInicial)
                .ativo(true)
                .criadoEm(LocalDateTime.of(2026, 3, 23, 16, 0))
                .build();

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(accountRepository.calcularSaldoReal(accountId, userId)).thenReturn(Optional.of(saldoCalculado));

        AccountSummaryComSaldo response = accountService.findByIdComSaldo(accountId, userId);

        assertEquals(accountId, response.id());
        assertEquals(saldoInicial, response.saldoInicial());
        assertEquals(saldoCalculado, response.saldoAtual());
        verify(accountRepository).findByIdAndUserId(accountId, userId);
        verify(accountRepository).calcularSaldoReal(accountId, userId);
    }

    @Test
    void usaSaldoInicialComoFallbackQuandoNaoHaResultadoCalculado() {
        Long accountId = 11L;
        Long userId = 21L;
        BigDecimal saldoInicial = new BigDecimal("250.00");

        Account account = Account.builder()
                .id(accountId)
                .nome("Reserva")
                .tipo("CONTA_CORRENTE")
                .saldoInicial(saldoInicial)
                .ativo(true)
                .criadoEm(LocalDateTime.of(2026, 3, 23, 16, 5))
                .build();

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(accountRepository.calcularSaldoReal(accountId, userId)).thenReturn(Optional.empty());

        AccountSummaryComSaldo response = accountService.findByIdComSaldo(accountId, userId);

        assertEquals(saldoInicial, response.saldoAtual());
        verify(accountRepository).findByIdAndUserId(accountId, userId);
        verify(accountRepository).calcularSaldoReal(accountId, userId);
    }
}
