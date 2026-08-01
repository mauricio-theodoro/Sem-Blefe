package br.com.semblefe.identity.application.model;

import java.time.Instant;
import java.util.Objects;

public record EmailVerificationDelivery(
        String destinationEmail,
        Instant expiresAt) {

    public EmailVerificationDelivery {
        if (destinationEmail == null || destinationEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "O destinatário da confirmação é obrigatório.");
        }

        Objects.requireNonNull(
                expiresAt,
                "A data de expiração da confirmação é obrigatória.");
    }

    @Override
    public String toString() {
        return "EmailVerificationDelivery[destinationEmail=[REDACTED], expiresAt=%s]"
                .formatted(expiresAt);
    }
}