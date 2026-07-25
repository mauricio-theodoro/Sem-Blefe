package br.com.semblefe.identity.domain;

import br.com.semblefe.shared.domain.BusinessValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void shouldAcceptLongPassphrase() {
        assertThatCode(() -> PasswordPolicy.validate(
                "Meu estúdio tem som e história!"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldPreservePasswordWhitespaceAndCase() {
        assertThatCode(() -> PasswordPolicy.validate(
                "  Minha frase secreta 2026!  "))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectShortPassword() {
        assertThatThrownBy(() -> PasswordPolicy.validate("Curta#2026"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("entre 15 e 128");
    }

    @Test
    void shouldRejectExcessivelyLongPassword() {
        assertThatThrownBy(() -> PasswordPolicy.validate("aB9!".repeat(33)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("entre 15 e 128");
    }

    @Test
    void shouldRejectObviousPassword() {
        assertThatThrownBy(() -> PasswordPolicy.validate("passwordpassword"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("menos previsível");
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThatThrownBy(() -> PasswordPolicy.validate(" ".repeat(20)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Informe uma senha.");
    }
}
