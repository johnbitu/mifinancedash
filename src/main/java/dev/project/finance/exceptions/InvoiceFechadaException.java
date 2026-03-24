package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvoiceFechadaException extends RuntimeException {
    public InvoiceFechadaException(String message) {
        super(message);
    }
}
