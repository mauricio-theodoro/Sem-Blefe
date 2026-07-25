package br.com.semblefe.identity.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentLegalDocumentsConfigurationAdapterTest {

    @Test
    void shouldNormalizeConfiguredVersions() {
        CurrentLegalDocumentsConfigurationAdapter documents =
                new CurrentLegalDocumentsConfigurationAdapter(
                        " 1.0 ",
                        " 2.0 ");

        assertThat(documents.termsOfUseVersion()).isEqualTo("1.0");
        assertThat(documents.privacyPolicyVersion()).isEqualTo("2.0");
    }

    @Test
    void shouldFailBeforeRegistrationWhenVersionExceedsDatabaseLimit() {
        assertThatThrownBy(
                () -> new CurrentLegalDocumentsConfigurationAdapter(
                        "v".repeat(31),
                        "1.0"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no máximo 30 caracteres");
    }
}
