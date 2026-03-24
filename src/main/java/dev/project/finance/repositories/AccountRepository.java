package dev.project.finance.repositories;

import dev.project.finance.models.Account;
import dev.project.finance.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("""
            select coalesce(sum(a.saldoAtual), 0)
            from Account a
            where a.user.id = :userId and a.ativo = true
            """)
    BigDecimal somarSaldoAtualAtivoPorUsuario(Long userId);
}
