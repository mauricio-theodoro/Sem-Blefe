package br.com.semblefe.shared.web;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String requestId,
        List<FieldViolation> fieldViolations) {

    public record FieldViolation(String field, String message) {
    }
}
