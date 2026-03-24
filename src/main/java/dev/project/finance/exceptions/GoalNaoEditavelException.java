package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GoalNaoEditavelException extends RuntimeException {
    public GoalNaoEditavelException(String message) {
        super(message);
    }
}
