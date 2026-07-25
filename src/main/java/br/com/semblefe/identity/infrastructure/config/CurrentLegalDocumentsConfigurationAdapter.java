package br.com.semblefe.identity.infrastructure.config;

import br.com.semblefe.identity.application.port.outbound.CurrentLegalDocuments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrentLegalDocumentsConfigurationAdapter
        implements CurrentLegalDocuments {

    private final String termsOfUseVersion;
    private final String privacyPolicyVersion;

    public CurrentLegalDocumentsConfigurationAdapter(
            @Value("${semblefe.legal-documents.terms-of-use-version}")
            String termsOfUseVersion,
            @Value("${semblefe.legal-documents.privacy-policy-version}")
            String privacyPolicyVersion) {
        this.termsOfUseVersion = validate(
                termsOfUseVersion,
                "Termos de Uso");
        this.privacyPolicyVersion = validate(
                privacyPolicyVersion,
                "Política de Privacidade");
    }

    @Override
    public String termsOfUseVersion() {
        return termsOfUseVersion;
    }

    @Override
    public String privacyPolicyVersion() {
        return privacyPolicyVersion;
    }

    private String validate(String version, String document) {
        if (version == null || version.isBlank()) {
            throw new IllegalStateException(
                    "A versão do documento %s não foi configurada."
                            .formatted(document));
        }

        String validatedVersion = version.strip();
        if (validatedVersion.length() > 30) {
            throw new IllegalStateException(
                    "A versão do documento %s deve possuir no máximo 30 caracteres."
                            .formatted(document));
        }

        return validatedVersion;
    }
}
