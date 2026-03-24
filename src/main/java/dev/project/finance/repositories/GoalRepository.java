package dev.project.finance.repositories;

import dev.project.finance.models.Goal;
import dev.project.finance.models.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    Optional<Goal> findByIdAndUserId(Long id, Long userId);

    List<Goal> findByUserIdOrderByDataPrazoAsc(Long userId);

    List<Goal> findByUserIdAndStatus(Long userId, GoalStatus status);

    List<Goal> findByStatusAndDataPrazoLessThan(GoalStatus status, LocalDate dataPrazo);
}
