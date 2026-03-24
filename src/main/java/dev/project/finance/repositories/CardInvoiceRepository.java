package dev.project.finance.repositories;

import dev.project.finance.models.CardInvoice;
import dev.project.finance.models.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardInvoiceRepository extends JpaRepository<CardInvoice, Long> {
    Optional<CardInvoice> findByCardIdAndMesReferenciaAndAnoReferencia(Long cardId, Integer mesReferencia, Integer anoReferencia);

    List<CardInvoice> findByUserIdAndStatusIn(Long userId, List<InvoiceStatus> statuses);

    List<CardInvoice> findByCardIdOrderByAnoReferenciaDescMesReferenciaDesc(Long cardId);

    Optional<CardInvoice> findByIdAndCardIdAndUserId(Long id, Long cardId, Long userId);

    boolean existsByCardIdAndStatusAndValorTotalGreaterThan(Long cardId, InvoiceStatus status, java.math.BigDecimal valor);
}
