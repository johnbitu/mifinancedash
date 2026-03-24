package dev.project.finance.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalRequest(
        @NotBlank(message = "O nome da meta e obrigatorio")
        @Size(max = 100, message = "O nome da meta deve ter no maximo 100 caracteres")
        String nome,
        String descricao,
        @NotNull(message = "O valor alvo e obrigatorio")
        BigDecimal valorAlvo,
        @NotNull(message = "A data de inicio e obrigatoria")
        LocalDate dataInicio,
        @NotNull(message = "A data prazo e obrigatoria")
        LocalDate dataPrazo,
        Long accountId
) {
}
