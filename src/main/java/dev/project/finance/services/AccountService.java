package dev.project.finance.services;

import dev.project.finance.dtos.CreateAccountRequest;
import dev.project.finance.models.Account;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account create(CreateAccountRequest request, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Account account = Account.builder()
                .nome(request.nome())
                .tipo(request.tipo())
                .saldoInicial(request.saldoInicial())
                .user(user)
                .build();


        return accountRepository.save(account);
    }

    public List<Account> findAllByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account findByIdAndUserId(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
    }

    @Transactional
    public Account update(Long accountId, Long userId, CreateAccountRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        account.setNome(request.nome());
        account.setTipo(request.tipo());
        account.setSaldoInicial(request.saldoInicial());

        return accountRepository.save(account);
    }

    @Transactional
    public void deactivate(Long accountId, Long userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        account.setAtivo(false);
        accountRepository.save(account);
    }
}
