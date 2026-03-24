package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class LimiteInsuficienteException extends RuntimeException {
    public LimiteInsuficienteException(String message) {
        super(message);
    }
}
