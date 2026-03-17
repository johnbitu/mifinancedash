package dev.project.finance.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(min = 2, max = 100)
        String nome,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Formato de email invalido")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
        String senha
) {
}
