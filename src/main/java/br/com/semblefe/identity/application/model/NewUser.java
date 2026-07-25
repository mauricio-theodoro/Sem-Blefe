package br.com.semblefe.identity.application.model;

import br.com.semblefe.identity.domain.Email;

import java.util.Objects;
import java.util.UUID;

/**
 * User data ready for persistence, without the original password.
 */
public record NewUser(
        UUID id,
        Email email,
        String passwordHash,
        String termsOfUseVersion,
        String privacyPolicyVersion) {

    public NewUser {
        Objects.requireNonNull(id);
        Objects.requireNonNull(email);

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("O hash da senha é obrigatório.");
        }

        if (termsOfUseVersion == null || termsOfUseVersion.isBlank()
                || privacyPolicyVersion == null
                || privacyPolicyVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "As versões dos documentos legais são obrigatórias.");
        }
    }

    public String initialStatus() {
        return "PENDENTE_EMAIL";
    }

    public boolean emailVerified() {
        return false;
    }

    public String initialRole() {
        return "USUARIO";
    }

    public String legalAcceptanceSource() {
        return "PWA";
    }
}
