package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvoiceNaoEncontradaException extends RuntimeException {
    public InvoiceNaoEncontradaException(String message) {
        super(message);
    }
}
