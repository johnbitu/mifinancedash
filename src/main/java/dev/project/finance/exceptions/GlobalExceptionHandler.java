package dev.project.finance.exceptions;

import dev.project.finance.models.Roles;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonError(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        String message = "JSON inválido";

        if (ex.getMostSpecificCause().getMessage().contains("Roles")) {
            String valores = Arrays.stream(Roles.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            message = "Role inválida. Valores permitidos: " + valores;
        }

        return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailError(
            EmailAlreadyInUseException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String message,
            String path
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        return ResponseEntity.status(status).body(error);
    }
}