package dev.project.finance.repositories;

import dev.project.finance.models.Recurrence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurrenceRepository extends JpaRepository<Recurrence, Long> {
    Optional<Recurrence> findByIdAndUserId(Long id, Long userId);

    List<Recurrence> findByUserIdOrderByProximaExecucaoAsc(Long userId);

    List<Recurrence> findByUserIdAndAtivo(Long userId, Boolean ativo);

    List<Recurrence> findByAtivoTrueAndProximaExecucaoLessThanEqual(LocalDate data);

    List<Recurrence> findTop5ByUserIdAndAtivoTrueOrderByProximaExecucaoAsc(Long userId);

    boolean existsByAccountIdAndAtivoTrue(Long accountId);

    boolean existsByCategoryIdAndAtivoTrue(Long categoryId);
}
