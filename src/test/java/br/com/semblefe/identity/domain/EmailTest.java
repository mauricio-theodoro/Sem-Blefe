package br.com.semblefe.identity.domain;

import br.com.semblefe.shared.domain.BusinessValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void shouldPreserveValueAndProduceNormalizedIdentity() {
        Email email = Email.of("  Artista@Exemplo.COM  ");

        assertThat(email.value()).isEqualTo("Artista@Exemplo.COM");
        assertThat(email.normalized()).isEqualTo("artista@exemplo.com");
    }

    @Test
    void shouldRejectMoreThanOneSeparator() {
        assertThatThrownBy(() -> Email.of("artista@@exemplo.com"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Informe um e-mail válido.");
    }

    @Test
    void shouldRejectInternalWhitespace() {
        assertThatThrownBy(() -> Email.of("artista\t@exemplo.com"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Informe um e-mail válido.");
    }

    @Test
    void shouldRejectInconsistentDirectConstruction() {
        assertThatThrownBy(() -> new Email(
                "Artista@Exemplo.COM",
                "outra-identidade@exemplo.com"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Informe um e-mail válido.");
    }
}
