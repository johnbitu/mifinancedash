package dev.project.finance.repositories;

import dev.project.finance.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByCategoryId(Long categoryId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
