package dev.project.finance.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> jsonInvalido(HttpMessageNotReadableException ex) {

        String mensagem = "JSON inválido";

        if (ex.getMostSpecificCause().getMessage().contains("Roles")) {
            mensagem = "Role inválida. Valores permitidos: ADMIN ou USUARIO";
        }

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "error", mensagem
                ));
    }
}