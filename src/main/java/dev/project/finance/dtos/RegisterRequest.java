package dev.project.finance.dtos;

import dev.project.finance.models.Roles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public record RegisterRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(min = 2, max = 100)
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
        String senha,

        @NotNull(message = "Role é obrigatória (ADMIN ou USUARIO)")
        Roles role
    ) {}
