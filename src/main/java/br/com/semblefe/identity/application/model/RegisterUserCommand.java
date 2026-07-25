package br.com.semblefe.identity.application.model;

import br.com.semblefe.shared.domain.BusinessValidationException;

public record RegisterUserCommand(
        String email,
        String password,
        String termsOfUseVersion,
        String privacyPolicyVersion,
        boolean acceptedTermsOfUse,
        boolean acceptedPrivacyPolicy) {

    public RegisterUserCommand {
        if (termsOfUseVersion == null || termsOfUseVersion.isBlank()) {
            throw new BusinessValidationException(
                    "LEGAL_DOCUMENT_VERSION_REQUIRED",
                    "termsOfUseVersion",
                    "Informe a versão dos Termos de Uso exibida.");
        }

        if (privacyPolicyVersion == null || privacyPolicyVersion.isBlank()) {
            throw new BusinessValidationException(
                    "LEGAL_DOCUMENT_VERSION_REQUIRED",
                    "privacyPolicyVersion",
                    "Informe a versão da Política de Privacidade exibida.");
        }

        if (!acceptedTermsOfUse) {
            throw new BusinessValidationException(
                    "REQUIRED_CONSENT",
                    "acceptedTermsOfUse",
                    "É necessário aceitar os Termos de Uso.");
        }

        if (!acceptedPrivacyPolicy) {
            throw new BusinessValidationException(
                    "REQUIRED_CONSENT",
                    "acceptedPrivacyPolicy",
                    "É necessário aceitar a Política de Privacidade.");
        }
    }
}
