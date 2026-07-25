package br.com.semblefe.shared.web;

import br.com.semblefe.shared.domain.BusinessValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<ApiError.FieldViolation> fieldViolations = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldViolation)
                .toList();

        ApiError error = newApiError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Verifique os campos informados.",
                request,
                fieldViolations);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ApiError error = newApiError(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "O corpo da requisição contém campos ou valores inválidos.",
                request,
                List.of());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BusinessValidationException.class)
    ResponseEntity<ApiError> handleBusinessValidation(
            BusinessValidationException exception,
            HttpServletRequest request) {

        List<ApiError.FieldViolation> fieldViolations = exception.getField() == null
                ? List.of()
                : List.of(new ApiError.FieldViolation(
                        exception.getField(),
                        exception.getMessage()));

        ApiError error = newApiError(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                "Verifique os campos informados.",
                request,
                fieldViolations);

        return ResponseEntity.badRequest().body(error);
    }

    private ApiError.FieldViolation toFieldViolation(FieldError error) {
        return new ApiError.FieldViolation(error.getField(), error.getDefaultMessage());
    }

    private ApiError newApiError(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ApiError.FieldViolation> fieldViolations) {

        return new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                MDC.get("requestId"),
                fieldViolations);
    }
}
