package br.com.semblefe.identity.api;

public record EmailVerificationResponse(
        String code,
        String message) {

    static EmailVerificationResponse emailVerified() {
        return new EmailVerificationResponse(
                "EMAIL_VERIFIED",
                "E-mail confirmado com sucesso. Sua conta está ativa.");
    }
}