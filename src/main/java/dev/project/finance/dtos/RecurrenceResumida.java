package dev.project.finance.dtos;

import java.time.LocalDate;

public record RecurrenceResumida(
        Long id,
        String nome,
        LocalDate proximaExecucao,
        String frequencia
) {
}
