package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ContaEmUsoException extends RuntimeException {
    public ContaEmUsoException(String message) {
        super(message);
    }
}
