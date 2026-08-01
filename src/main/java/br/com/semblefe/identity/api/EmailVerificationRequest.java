package br.com.semblefe.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationRequest(
        @NotBlank(message = "Informe o token de confirmação.")
        @Pattern(
                regexp = "[A-Za-z0-9_-]{43}",
                message = "O token de confirmação possui formato inválido.")
        String token) {

    @Override
    public String toString() {
        return "EmailVerificationRequest[token=[REDACTED]]";
    }
}