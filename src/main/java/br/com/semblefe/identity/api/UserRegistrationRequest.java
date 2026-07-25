package br.com.semblefe.identity.api;

import br.com.semblefe.identity.api.validation.ValidPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 254, message = "O e-mail deve possuir no máximo 254 caracteres.")
        String email,

        @ValidPassword
        String password,

        @NotBlank(message = "Informe a versão dos Termos de Uso exibida.")
        @Size(max = 30, message = "A versão dos Termos de Uso é inválida.")
        String termsOfUseVersion,

        @NotBlank(message = "Informe a versão da Política de Privacidade exibida.")
        @Size(max = 30, message = "A versão da Política de Privacidade é inválida.")
        String privacyPolicyVersion,

        @NotNull(message = "Informe o aceite dos Termos de Uso.")
        @AssertTrue(message = "É necessário aceitar os Termos de Uso.")
        Boolean acceptedTermsOfUse,

        @NotNull(message = "Informe o aceite da Política de Privacidade.")
        @AssertTrue(message = "É necessário aceitar a Política de Privacidade.")
        Boolean acceptedPrivacyPolicy) {

    public UserRegistrationRequest {
        if (email != null) {
            email = email.strip();
        }
    }
}
