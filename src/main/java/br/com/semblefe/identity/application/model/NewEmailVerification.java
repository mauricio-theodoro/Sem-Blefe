package br.com.semblefe.identity.application.model;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Verification data ready for persistence. The raw token is intentionally
 * absent so it cannot be written to the database by this contract.
 */
public record NewEmailVerification(
        UUID id,
        UUID userId,
        String tokenHash,
        Duration tokenValidity) {

    private static final Duration MINIMUM_VALIDITY =
            Duration.ofSeconds(1);

    private static final Pattern SHA_256_HEX =
            Pattern.compile("[0-9a-f]{64}");

    public NewEmailVerification {
        Objects.requireNonNull(
                id,
                "O identificador do token é obrigatório.");

        Objects.requireNonNull(
                userId,
                "O identificador do usuário é obrigatório.");

        Objects.requireNonNull(
                tokenValidity,
                "A validade do token é obrigatória.");

        if (tokenHash == null || !SHA_256_HEX.matcher(tokenHash).matches()) {
            throw new IllegalArgumentException(
                    "O hash do token de confirmação é inválido.");
        }

        if (tokenValidity.compareTo(MINIMUM_VALIDITY) < 0) {
            throw new IllegalArgumentException(
                    "A validade do token deve ser de pelo menos um segundo.");
        }
    }

    @Override
    public String toString() {
        return "NewEmailVerification[id=%s, userId=%s, tokenHash=[REDACTED], tokenValidity=%s]"
                .formatted(id, userId, tokenValidity);
    }
}