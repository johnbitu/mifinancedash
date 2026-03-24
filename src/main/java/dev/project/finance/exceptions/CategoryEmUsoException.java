package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CategoryEmUsoException extends RuntimeException {
    public CategoryEmUsoException(String message) {
        super(message);
    }
}
