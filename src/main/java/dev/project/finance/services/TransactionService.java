package dev.project.finance.services;

import dev.project.finance.dtos.CreateTransactionRequest;
import dev.project.finance.dtos.TransactionSummary;
import dev.project.finance.exceptions.AccountNotFoundException;
import dev.project.finance.exceptions.CardNotFoundException;
import dev.project.finance.exceptions.CategoryNotFoundException;
import dev.project.finance.exceptions.TransactionNotFoundException;
import dev.project.finance.models.Account;
import dev.project.finance.models.Card;
import dev.project.finance.models.Category;
import dev.project.finance.models.Transaction;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.CardRepository;
import dev.project.finance.repositories.CategoryRepository;
import dev.project.finance.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    @Transactional
    public TransactionSummary create(CreateTransactionRequest request, User usuarioAutenticado) {
        Account account = buscarContaPorIdEUsuario(request.accountId(), usuarioAutenticado.getId());
        Category category = buscarCategoriaOpcional(request.categoryId(), usuarioAutenticado.getId());
        Card card = buscarCardOpcional(request.cardId(), usuarioAutenticado.getId());

        Transaction transaction = Transaction.builder()
                .tipo(request.tipo())
                .valor(request.valor())
                .descricao(request.descricao())
                .dataTransacao(request.dataTransacao())
                .observacao(request.observacao())
                .user(usuarioAutenticado)
                .account(account)
                .category(category)
                .card(card)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        if (saved.getCard() != null) {
            cardService.processarTransacao(saved);
        }

        recalcularSaldo(saved.getAccount());
        accountRepository.save(saved.getAccount());

        return toSummary(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionSummary> findAllByUsuario(User usuarioAutenticado) {
        return transactionRepository.findByUserId(usuarioAutenticado.getId())
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionSummary findByIdAndUsuario(Long transactionId, User usuarioAutenticado) {
        return toSummary(buscarTransacaoPorIdEUsuario(transactionId, usuarioAutenticado.getId()));
    }

    @Transactional
    public TransactionSummary update(Long transactionId, CreateTransactionRequest request, User usuarioAutenticado) {
        Transaction transaction = buscarTransacaoPorIdEUsuario(transactionId, usuarioAutenticado.getId());
        if (transaction.getCard() != null) {
            cardService.reverterTransacao(transaction);
        }

        Account account = buscarContaPorIdEUsuario(request.accountId(), usuarioAutenticado.getId());
        Category category = buscarCategoriaOpcional(request.categoryId(), usuarioAutenticado.getId());
        Card card = buscarCardOpcional(request.cardId(), usuarioAutenticado.getId());

        transaction.setTipo(request.tipo());
        transaction.setValor(request.valor());
        transaction.setDescricao(request.descricao());
        transaction.setDataTransacao(request.dataTransacao());
        transaction.setObservacao(request.observacao());
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setCard(card);

        Transaction saved = transactionRepository.save(transaction);
        if (saved.getCard() != null) {
            cardService.processarTransacao(saved);
        }

        recalcularSaldo(saved.getAccount());
        accountRepository.save(saved.getAccount());

        return toSummary(saved);
    }

    @Transactional
    public void delete(Long transactionId, User usuarioAutenticado) {
        Transaction transaction = buscarTransacaoPorIdEUsuario(transactionId, usuarioAutenticado.getId());

        if (transaction.getCard() != null) {
            cardService.reverterTransacao(transaction);
        }

        Account account = transaction.getAccount();
        transactionRepository.delete(transaction);
        recalcularSaldo(account);
        accountRepository.save(account);
    }

    private Transaction buscarTransacaoPorIdEUsuario(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transacao nao encontrada: id=" + transactionId));
    }

    private Account buscarContaPorIdEUsuario(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserIdAndAtivoTrue(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta nao encontrada: id=" + accountId));
    }

    private Category buscarCategoriaOpcional(Long categoryId, Long userId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria nao encontrada: id=" + categoryId));
    }

    private Card buscarCardOpcional(Long cardId, Long userId) {
        if (cardId == null) {
            return null;
        }

        return cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException("Cartao nao encontrado para usuario"));
    }

    private void recalcularSaldo(Account account) {
        BigDecimal somatorio = transactionRepository.somatorioPorConta(account.getId());
        account.setSaldoAtual(account.getSaldoInicial().add(somatorio));
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
                transaction.getCategory() != null ? transaction.getCategory().getNome() : null,
                transaction.getCard() != null ? transaction.getCard().getId() : null,
                transaction.getCard() != null ? transaction.getCard().getNome() : null,
                transaction.getRecurrence() != null ? transaction.getRecurrence().getId() : null
        );
    }
}
