package dev.project.finance.services;

import dev.project.finance.dtos.CreateTransactionRequest;
import dev.project.finance.dtos.TransactionSummary;
import dev.project.finance.exceptions.AccountNotFoundException;
import dev.project.finance.exceptions.CategoryNotFoundException;
import dev.project.finance.exceptions.TransactionNotFoundException;
import dev.project.finance.models.Account;
import dev.project.finance.models.Category;
import dev.project.finance.models.Transaction;
import dev.project.finance.models.TransactionType;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.CategoryRepository;
import dev.project.finance.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionSummary create(CreateTransactionRequest request, User usuarioAutenticado) {
        Account account = buscarContaPorIdEUsuario(request.accountId(), usuarioAutenticado.getId());

        Category category = null;
        if (request.categoryId() != null) {
            category = buscarCategoriaPorIdEUsuario(request.categoryId(), usuarioAutenticado.getId());
        }

        Transaction transaction = Transaction.builder()
                .tipo(TransactionType.valueOf(request.tipo().toUpperCase()))
                .valor(request.valor())
                .descricao(request.descricao())
                .dataTransacao(request.dataTransacao())
                .observacao(request.observacao())
                .criadoEm(LocalDateTime.now())
                .user(usuarioAutenticado)
                .account(account)
                .category(category)
                .build();

        return toSummary(transactionRepository.save(transaction));
    }

    public List<TransactionSummary> findAllByUsuario(User usuarioAutenticado) {
        return transactionRepository.findByUserId(usuarioAutenticado.getId())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public TransactionSummary findByIdAndUsuario(Long transactionId, User usuarioAutenticado) {
        return toSummary(buscarTransacaoPorIdEUsuario(transactionId, usuarioAutenticado.getId()));
    }

    public TransactionSummary update(Long transactionId, CreateTransactionRequest request, User usuarioAutenticado) {
        Transaction transaction = buscarTransacaoPorIdEUsuario(transactionId, usuarioAutenticado.getId());

        Account account = buscarContaPorIdEUsuario(request.accountId(), usuarioAutenticado.getId());
        Category category = null;
        if (request.categoryId() != null) {
            category = buscarCategoriaPorIdEUsuario(request.categoryId(), usuarioAutenticado.getId());
        }

        transaction.setTipo(TransactionType.valueOf(request.tipo().toUpperCase()));
        transaction.setValor(request.valor());
        transaction.setDescricao(request.descricao());
        transaction.setDataTransacao(request.dataTransacao());
        transaction.setObservacao(request.observacao());
        transaction.setAccount(account);
        transaction.setCategory(category);

        return toSummary(transactionRepository.save(transaction));
    }

    public void delete(Long transactionId, User usuarioAutenticado) {
        Transaction transaction = buscarTransacaoPorIdEUsuario(transactionId, usuarioAutenticado.getId());
        transactionRepository.delete(transaction);
    }

    private Transaction buscarTransacaoPorIdEUsuario(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transacao nao encontrada: id=" + transactionId));
    }

    private Account buscarContaPorIdEUsuario(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta nao encontrada: id=" + accountId));
    }

    private Category buscarCategoriaPorIdEUsuario(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria nao encontrada: id=" + categoryId));
    }

    public TransactionSummary toSummary(Transaction transaction) {
        return new TransactionSummary(
                transaction.getId(),
                transaction.getTipo(),
                transaction.getValor(),
                transaction.getDescricao(),
                transaction.getDataTransacao(),
                transaction.getObservacao(),
                transaction.getCriadoEm(),
                transaction.getAccount().getId(),
                transaction.getAccount().getNome(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getCategory() != null ? transaction.getCategory().getNome() : null
        );
    }
}
