package dev.project.finance.services;

import dev.project.finance.dtos.AccountSummary;
import dev.project.finance.dtos.AccountSummaryComSaldo;
import dev.project.finance.dtos.CreateAccountRequest;
import dev.project.finance.dtos.UpdateAccountRequest;
import dev.project.finance.exceptions.AccountNotFoundException;
import dev.project.finance.exceptions.UserNotFoundException;
import dev.project.finance.models.Account;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountSummary create(CreateAccountRequest request, Long userId) {
        User user = buscarUsuarioPorId(userId);

        Account account = Account.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .saldoInicial(request.saldoInicial())
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .user(user)
                .build();

        return toSummary(accountRepository.save(account));
    }

    public List<AccountSummary> findAllByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public AccountSummary findByIdAndUserId(Long accountId, Long userId) {
        return toSummary(buscarContaPorIdEUsuario(accountId, userId));
    }

    @Transactional
    public AccountSummary update(Long accountId, Long userId, UpdateAccountRequest request) {
        Account account = buscarContaPorIdEUsuario(accountId, userId);

        account.setNome(request.nome());
        account.setTipo(request.tipo());
        account.setSaldoInicial(request.saldoInicial());
        account.setAtivo(request.ativo());

        return toSummary(accountRepository.save(account));
    }

    @Transactional
    public void deactivate(Long accountId, Long userId) {
        Account account = buscarContaPorIdEUsuario(accountId, userId);
        account.setAtivo(false);
        accountRepository.save(account);
    }

    // — Método novo: retorna a conta com saldo calculado —

    public AccountSummaryComSaldo findByIdComSaldo(Long accountId, Long userId) {
        Account account = buscarContaPorIdEUsuario(accountId, userId);

        BigDecimal saldoAtual = accountRepository
                .calcularSaldoReal(accountId, userId)
                .orElse(account.getSaldoInicial()); // fallback: sem transações = saldo inicial

        return toSummaryComSaldo(account, saldoAtual);
    }


    private User buscarUsuarioPorId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado"));
    }

    private Account buscarContaPorIdEUsuario(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta nao encontrada"));
    }

    public AccountSummary toSummary(Account account) {
        return new AccountSummary(
                account.getId(),
                account.getNome(),
                account.getTipo(),
                account.getSaldoInicial(),
                account.getAtivo(),
                account.getCriadoEm()
        );
    }

    private AccountSummaryComSaldo toSummaryComSaldo(Account account, BigDecimal saldoAtual) {
        return new AccountSummaryComSaldo(
                account.getId(),
                account.getNome(),
                account.getTipo(),
                account.getSaldoInicial(),
                saldoAtual,
                account.getAtivo(),
                account.getCriadoEm()
        );
    }
}
