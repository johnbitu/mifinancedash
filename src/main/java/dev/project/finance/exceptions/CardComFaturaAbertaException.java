package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CardComFaturaAbertaException extends RuntimeException {
    public CardComFaturaAbertaException(String message) {
        super(message);
    }
}
