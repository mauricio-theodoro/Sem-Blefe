package br.com.semblefe.identity.application.model;

import br.com.semblefe.shared.domain.BusinessValidationException;

import java.util.regex.Pattern;

public record VerifyEmailCommand(
        String token,
        String requestId) {

    private static final Pattern TOKEN_FORMAT =
            Pattern.compile("[A-Za-z0-9_-]{43}");

    private static final Pattern REQUEST_ID_FORMAT =
            Pattern.compile("[A-Za-z0-9._-]{1,64}");

    public VerifyEmailCommand {
        if (token == null || !TOKEN_FORMAT.matcher(token).matches()) {
            throw invalidToken();
        }

        if (requestId == null
                || !REQUEST_ID_FORMAT.matcher(requestId).matches()) {
            throw new IllegalArgumentException(
                    "O identificador da requisição é inválido.");
        }
    }

    public static BusinessValidationException invalidToken() {
        return new BusinessValidationException(
                "INVALID_OR_EXPIRED_EMAIL_VERIFICATION",
                "token",
                "O link de confirmação é inválido ou expirou.");
    }

    @Override
    public String toString() {
        return "VerifyEmailCommand[token=[REDACTED], requestId=%s]"
                .formatted(requestId);
    }
}