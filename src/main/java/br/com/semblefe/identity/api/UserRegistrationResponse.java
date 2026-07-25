package br.com.semblefe.identity.api;

public record UserRegistrationResponse(
        String code,
        String message) {

    static UserRegistrationResponse registrationReceived() {
        return new UserRegistrationResponse(
                "REGISTRATION_RECEIVED",
                "Cadastro recebido. A confirmação do endereço será necessária antes do acesso.");
    }
}
