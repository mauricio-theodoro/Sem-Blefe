package br.com.semblefe.identity.application.port.outbound;

import java.time.Instant;

public interface EmailVerificationNotifier {

    /**
     * Delivers the only copy of the raw token. Implementations must never log
     * or persist it.
     */
    void send(
            String destinationEmail,
            String rawToken,
            Instant expiresAt);
}