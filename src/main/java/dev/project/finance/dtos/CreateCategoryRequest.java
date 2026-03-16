package dev.project.finance.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "O nome da categoria é obrigatório")
        @Size(max = 100, message = "O nome da categoria deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O tipo da categoria é obrigatório")
        @Size(max = 20, message = "O tipo da categoria deve ter no máximo 20 caracteres")
        String tipo
) {}
