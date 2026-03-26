package dev.project.finance.services;

import dev.project.finance.dtos.CardInvoiceSummary;
import dev.project.finance.dtos.CardSummary;
import dev.project.finance.dtos.CreateCardRequest;
import dev.project.finance.dtos.UpdateCardInvoiceRequest;
import dev.project.finance.exceptions.AccountNotFoundException;
import dev.project.finance.exceptions.CardComFaturaAbertaException;
import dev.project.finance.exceptions.CardNotFoundException;
import dev.project.finance.exceptions.InvoiceFechadaException;
import dev.project.finance.exceptions.InvoiceNaoEncontradaException;
import dev.project.finance.exceptions.LimiteInsuficienteException;
import dev.project.finance.models.Account;
import dev.project.finance.models.Card;
import dev.project.finance.models.CardInvoice;
import dev.project.finance.models.CardType;
import dev.project.finance.models.InvoiceStatus;
import dev.project.finance.models.Transaction;
import dev.project.finance.models.TransactionType;
import dev.project.finance.models.User;
import dev.project.finance.repositories.AccountRepository;
import dev.project.finance.repositories.CardInvoiceRepository;
import dev.project.finance.repositories.CardRepository;
import dev.project.finance.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardInvoiceRepository cardInvoiceRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public CardSummary create(CreateCardRequest request, User user) {
        Account account = buscarContaOpcional(request.accountId(), user.getId());
        validarRegrasCartao(request.tipo(), request.limite(), request.diaFechamento(), request.diaVencimento());

        BigDecimal limite = request.tipo() == CardType.CREDITO ? request.limite() : null;
        BigDecimal limiteDisponivel = request.tipo() == CardType.CREDITO ? limite : null;

        Card card = Card.builder()
                .user(user)
                .account(account)
                .nome(request.nome())
                .bandeira(request.bandeira())
                .tipo(request.tipo())
                .limite(limite)
                .limiteDisponivel(limiteDisponivel)
                .diaFechamento(request.tipo() == CardType.CREDITO ? request.diaFechamento() : null)
                .diaVencimento(request.tipo() == CardType.CREDITO ? request.diaVencimento() : null)
                .ativo(true)
                .build();

        return toSummary(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardSummary> listar(Long userId) {
        return cardRepository.findByUserIdOrderByNomeAsc(userId).stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public CardSummary buscar(Long id, Long userId) {
        return toSummary(buscarCartao(id, userId));
    }

    @Transactional
    public CardSummary atualizar(Long id, Long userId, CreateCardRequest request) {
        Card card = buscarCartao(id, userId);
        validarRegrasCartao(request.tipo(), request.limite(), request.diaFechamento(), request.diaVencimento());

        card.setNome(request.nome());
        card.setBandeira(request.bandeira());
        card.setTipo(request.tipo());
        card.setAccount(buscarContaOpcional(request.accountId(), userId));

        if (request.tipo() == CardType.CREDITO) {
            card.setLimite(request.limite());
            card.setDiaFechamento(request.diaFechamento());
            card.setDiaVencimento(request.diaVencimento());
            recalcularLimiteDisponivel(card);
        } else {
            card.setLimite(null);
            card.setLimiteDisponivel(null);
            card.setDiaFechamento(null);
            card.setDiaVencimento(null);
        }

        return toSummary(cardRepository.save(card));
    }

    @Transactional
    public void desativar(Long id, Long userId) {
        Card card = buscarCartao(id, userId);
        boolean faturaAbertaComValor = cardInvoiceRepository
                .existsByCardIdAndStatusAndValorTotalGreaterThan(card.getId(), InvoiceStatus.ABERTA, BigDecimal.ZERO);
        if (faturaAbertaComValor) {
            throw new CardComFaturaAbertaException("Nao e possivel desativar cartao com fatura aberta e valor pendente");
        }

        card.setAtivo(false);
        cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public List<CardInvoiceSummary> listarFaturas(Long cardId, Long userId) {
        buscarCartao(cardId, userId);
        return cardInvoiceRepository.findByCardIdOrderByAnoReferenciaDescMesReferenciaDesc(cardId)
                .stream()
                .map(this::toInvoiceSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public CardInvoiceSummary buscarFatura(Long cardId, Long invoiceId, Long userId) {
        CardInvoice invoice = cardInvoiceRepository.findByIdAndCardIdAndUserId(invoiceId, cardId, userId)
                .orElseThrow(() -> new InvoiceNaoEncontradaException("Fatura nao encontrada"));
        return toInvoiceSummary(invoice);
    }

    @Transactional
    public CardInvoiceSummary atualizarFatura(Long cardId, Long invoiceId, Long userId, UpdateCardInvoiceRequest request) {
        CardInvoice invoice = cardInvoiceRepository.findByIdAndCardIdAndUserId(invoiceId, cardId, userId)
                .orElseThrow(() -> new InvoiceNaoEncontradaException("Fatura nao encontrada"));

        Integer mesReferencia = request.mesReferencia() != null ? request.mesReferencia() : invoice.getMesReferencia();
        Integer anoReferencia = request.anoReferencia() != null ? request.anoReferencia() : invoice.getAnoReferencia();
        validarDuplicidadeFatura(invoice, mesReferencia, anoReferencia);

        invoice.setMesReferencia(mesReferencia);
        invoice.setAnoReferencia(anoReferencia);

        if (request.valorTotal() != null) {
            invoice.setValorTotal(request.valorTotal());
        }
        if (request.dataVencimento() != null) {
            invoice.setDataVencimento(request.dataVencimento());
        }
        if (request.status() != null) {
            invoice.setStatus(request.status());
        }

        cardInvoiceRepository.save(invoice);
        recalcularLimiteDisponivel(invoice.getCard());
        cardRepository.save(invoice.getCard());
        return toInvoiceSummary(invoice);
    }

    @Transactional
    public CardInvoiceSummary fecharFatura(Long cardId, Long invoiceId, Long userId) {
        CardInvoice invoice = cardInvoiceRepository.findByIdAndCardIdAndUserId(invoiceId, cardId, userId)
                .orElseThrow(() -> new InvoiceNaoEncontradaException("Fatura nao encontrada"));
        if (invoice.getStatus() != InvoiceStatus.ABERTA) {
            throw new InvoiceFechadaException("Apenas faturas abertas podem ser fechadas");
        }

        invoice.setStatus(InvoiceStatus.FECHADA);
        cardInvoiceRepository.save(invoice);

        gerarTransacaoFechamento(invoice);
        return toInvoiceSummary(invoice);
    }

    @Transactional
    public CardInvoiceSummary pagarFatura(Long cardId, Long invoiceId, Long userId) {
        CardInvoice invoice = cardInvoiceRepository.findByIdAndCardIdAndUserId(invoiceId, cardId, userId)
                .orElseThrow(() -> new InvoiceNaoEncontradaException("Fatura nao encontrada"));

        if (invoice.getStatus() != InvoiceStatus.FECHADA) {
            throw new InvoiceFechadaException("A fatura precisa estar FECHADA para ser paga");
        }

        invoice.setStatus(InvoiceStatus.PAGA);
        cardInvoiceRepository.save(invoice);

        recalcularLimiteDisponivel(invoice.getCard());
        cardRepository.save(invoice.getCard());

        return toInvoiceSummary(invoice);
    }

    @Transactional
    public void processarTransacao(Transaction transaction) {
        Card card = transaction.getCard();
        if (card == null) {
            return;
        }

        if (!Boolean.TRUE.equals(card.getAtivo())) {
            throw new CardNotFoundException("Cartao inativo ou indisponivel");
        }

        CardInvoice invoice = findOrCreateInvoice(card, transaction.getUser(), transaction.getDataTransacao());
        if (invoice.getStatus() == InvoiceStatus.FECHADA) {
            throw new InvoiceFechadaException("Fatura fechada nao aceita novas transacoes");
        }

        if (card.getTipo() == CardType.CREDITO) {
            if (card.getLimiteDisponivel() == null || card.getLimiteDisponivel().compareTo(transaction.getValor()) < 0) {
                throw new LimiteInsuficienteException("Limite disponivel insuficiente para a transacao");
            }
        }

        invoice.setValorTotal(invoice.getValorTotal().add(transaction.getValor()));
        cardInvoiceRepository.save(invoice);

        recalcularLimiteDisponivel(card);
        cardRepository.save(card);
    }

    @Transactional
    public void reverterTransacao(Transaction transaction) {
        Card card = transaction.getCard();
        if (card == null) {
            return;
        }

        YearMonth ref = YearMonth.from(transaction.getDataTransacao());
        cardInvoiceRepository.findByCardIdAndMesReferenciaAndAnoReferencia(card.getId(), ref.getMonthValue(), ref.getYear())
                .ifPresent(invoice -> {
                    BigDecimal novoValor = invoice.getValorTotal().subtract(transaction.getValor());
                    invoice.setValorTotal(novoValor.max(BigDecimal.ZERO));

                    if (invoice.getValorTotal().compareTo(BigDecimal.ZERO) == 0 && invoice.getStatus() == InvoiceStatus.ABERTA) {
                        cardInvoiceRepository.delete(invoice);
                    } else {
                        cardInvoiceRepository.save(invoice);
                    }
                });

        recalcularLimiteDisponivel(card);
        cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public Card buscarCartaoParaUsuario(Long cardId, Long userId) {
        return buscarCartao(cardId, userId);
    }

    private void validarRegrasCartao(CardType tipo,
                                     BigDecimal limite,
                                     Integer diaFechamento,
                                     Integer diaVencimento) {
        if (tipo == CardType.CREDITO) {
            if (limite == null || limite.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Limite e obrigatorio para cartao de credito");
            }
            if (diaFechamento == null || diaVencimento == null) {
                throw new IllegalArgumentException("Dia de fechamento e vencimento sao obrigatorios para credito");
            }
        }
    }

    private Card buscarCartao(Long id, Long userId) {
        return cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CardNotFoundException("Cartao nao encontrado"));
    }

    private Account buscarContaOpcional(Long accountId, Long userId) {
        if (accountId == null) {
            return null;
        }

        return accountRepository.findByIdAndUserIdAndAtivoTrue(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta nao encontrada para vinculacao"));
    }

    private void validarDuplicidadeFatura(CardInvoice invoice, Integer mesReferencia, Integer anoReferencia) {
        cardInvoiceRepository.findByCardIdAndMesReferenciaAndAnoReferencia(invoice.getCard().getId(), mesReferencia, anoReferencia)
                .filter(existing -> !existing.getId().equals(invoice.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ja existe fatura para o cartao no mes/ano informado");
                });
    }

    private CardInvoice findOrCreateInvoice(Card card, User user, LocalDate dataTransacao) {
        YearMonth ref = YearMonth.from(dataTransacao);
        return cardInvoiceRepository.findByCardIdAndMesReferenciaAndAnoReferencia(
                        card.getId(),
                        ref.getMonthValue(),
                        ref.getYear()
                )
                .orElseGet(() -> {
                    int diaVencimento = card.getDiaVencimento() == null ? 1 : card.getDiaVencimento();
                    LocalDate vencimento = LocalDate.of(ref.getYear(), ref.getMonth(),
                            Math.min(diaVencimento, ref.lengthOfMonth()));
                    CardInvoice invoice = CardInvoice.builder()
                            .card(card)
                            .user(user)
                            .mesReferencia(ref.getMonthValue())
                            .anoReferencia(ref.getYear())
                            .valorTotal(BigDecimal.ZERO)
                            .status(InvoiceStatus.ABERTA)
                            .dataVencimento(vencimento)
                            .build();
                    return cardInvoiceRepository.save(invoice);
                });
    }

    private void gerarTransacaoFechamento(CardInvoice invoice) {
        Card card = invoice.getCard();
        if (card.getAccount() == null || invoice.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Transaction transaction = Transaction.builder()
                .user(invoice.getUser())
                .account(card.getAccount())
                .category(null)
                .card(null)
                .recurrence(null)
                .tipo(TransactionType.DESPESA)
                .valor(invoice.getValorTotal())
                .descricao("Fatura " + card.getNome() + " " + invoice.getMesReferencia() + "/" + invoice.getAnoReferencia())
                .dataTransacao(LocalDate.now())
                .build();

        transactionRepository.save(transaction);
        recalcularSaldo(card.getAccount());
        accountRepository.save(card.getAccount());
    }

    private void recalcularLimiteDisponivel(Card card) {
        if (card.getTipo() != CardType.CREDITO || card.getLimite() == null) {
            card.setLimiteDisponivel(null);
            return;
        }

        BigDecimal totalAberto = cardInvoiceRepository.findByCardIdOrderByAnoReferenciaDescMesReferenciaDesc(card.getId())
                .stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.ABERTA || invoice.getStatus() == InvoiceStatus.FECHADA)
                .map(CardInvoice::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        card.setLimiteDisponivel(card.getLimite().subtract(totalAberto));
    }

    private void recalcularSaldo(Account account) {
        BigDecimal somatorio = transactionRepository.somatorioPorConta(account.getId());
        account.setSaldoAtual(account.getSaldoInicial().add(somatorio));
    }

    public CardSummary toSummary(Card card) {
        return new CardSummary(
                card.getId(),
                card.getNome(),
                card.getBandeira(),
                card.getTipo(),
                card.getAccount() != null ? card.getAccount().getId() : null,
                card.getAccount() != null ? card.getAccount().getNome() : null,
                card.getLimite(),
                card.getLimiteDisponivel(),
                card.getDiaFechamento(),
                card.getDiaVencimento(),
                card.getAtivo(),
                card.getCriadoEm()
        );
    }

    public CardInvoiceSummary toInvoiceSummary(CardInvoice invoice) {
        return new CardInvoiceSummary(
                invoice.getId(),
                invoice.getCard().getId(),
                invoice.getCard().getNome(),
                invoice.getMesReferencia(),
                invoice.getAnoReferencia(),
                invoice.getValorTotal(),
                invoice.getDataVencimento(),
                invoice.getStatus(),
                invoice.getCriadoEm()
        );
    }
}
