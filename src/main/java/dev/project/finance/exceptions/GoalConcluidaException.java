package dev.project.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GoalConcluidaException extends RuntimeException {
    public GoalConcluidaException(String message) {
        super(message);
    }
}
