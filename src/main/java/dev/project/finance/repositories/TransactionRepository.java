package dev.project.finance.repositories;

import dev.project.finance.models.Transaction;
import dev.project.finance.models.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select t
            from Transaction t
            left join fetch t.account a
            left join fetch t.category c
            left join fetch t.card card
            where t.user.id = :userId
            order by t.dataTransacao desc, t.id desc
            """)
    List<Transaction> findByUserId(@Param("userId") Long userId);

    List<Transaction> findByAccountId(Long accountId);

    boolean existsByAccountIdAndDataTransacaoAfter(Long accountId, LocalDate data);

    List<Transaction> findByCategoryId(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    List<Transaction> findByRecurrenceId(Long recurrenceId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("""
            select t
            from Transaction t
            left join fetch t.account a
            left join fetch t.category c
            where t.user.id = :userId
            and t.dataTransacao between :inicio and :fim
            order by t.dataTransacao desc, t.id desc
            """)
    List<Transaction> findByUserIdAndDataTransacaoBetween(
            @Param("userId") Long userId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
            select coalesce(sum(t.valor), 0)
            from Transaction t
            where t.user.id = :userId
            and t.tipo = :tipo
            and t.dataTransacao between :inicio and :fim
            """)
    BigDecimal sumValorByUserIdAndTipoAndDataTransacaoBetween(
            @Param("userId") Long userId,
            @Param("tipo") TransactionType tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
            select coalesce(sum(case when t.tipo = dev.project.finance.models.TransactionType.RECEITA then t.valor else -t.valor end), 0)
            from Transaction t
            where t.account.id = :accountId
            """)
    BigDecimal somatorioPorConta(@Param("accountId") Long accountId);
}
