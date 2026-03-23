package dev.project.finance.repositories;

import dev.project.finance.models.Account;
import dev.project.finance.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository <Account, Long> {
    List<Account> findByUser(User user);

    List<Account> findByUserId(Long userId);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

    Optional<Account> findByIdAndUserIdAndAtivoTrue(Long id, Long userId);


    // Calcula o saldo real: saldo_inicial + receitas - despesas
    @Query("""
        SELECT a.saldoInicial
             + COALESCE(SUM(CASE WHEN t.tipo = 'RECEITA' THEN t.valor ELSE 0 END), 0)
             - COALESCE(SUM(CASE WHEN t.tipo = 'DESPESA' THEN t.valor ELSE 0 END), 0)
        FROM Account a
        LEFT JOIN Transaction t ON t.account = a
        WHERE a.id = :accountId
          AND a.user.id = :userId
        GROUP BY a.saldoInicial
        """)
    Optional<BigDecimal> calcularSaldoReal(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId
    );
}
