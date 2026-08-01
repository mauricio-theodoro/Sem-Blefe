package br.com.semblefe.identity.application;

import br.com.semblefe.identity.application.model.VerifyEmailCommand;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationPersistence;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationTokenSecurity;
import br.com.semblefe.shared.domain.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifyEmailServiceTest {

    private static final String RAW_TOKEN = "a".repeat(43);
    private static final String TOKEN_HASH = "b".repeat(64);
    private static final String REQUEST_ID =
            "verification-service-001";

    private EmailVerificationTokenSecurity tokenSecurity;
    private EmailVerificationPersistence verificationPersistence;
    private VerifyEmailService service;

    @BeforeEach
    void setUp() {
        tokenSecurity =
                mock(EmailVerificationTokenSecurity.class);

        verificationPersistence =
                mock(EmailVerificationPersistence.class);

        service = new VerifyEmailService(
                tokenSecurity,
                verificationPersistence);

        when(tokenSecurity.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);
    }

    @Test
    void shouldConfirmUsingOnlyTheTokenHash() {
        when(verificationPersistence.verify(
                TOKEN_HASH,
                REQUEST_ID))
                .thenReturn(true);

        assertThatCode(() -> service.execute(
                new VerifyEmailCommand(
                        RAW_TOKEN,
                        REQUEST_ID)))
                .doesNotThrowAnyException();

        verify(tokenSecurity).hash(RAW_TOKEN);

        verify(verificationPersistence).verify(
                TOKEN_HASH,
                REQUEST_ID);
    }

    @Test
    void shouldReturnNeutralFailureWithoutExposingToken() {
        when(verificationPersistence.verify(
                TOKEN_HASH,
                REQUEST_ID))
                .thenReturn(false);

        assertThatThrownBy(() -> service.execute(
                new VerifyEmailCommand(
                        RAW_TOKEN,
                        REQUEST_ID)))
                .isInstanceOfSatisfying(
                        BusinessValidationException.class,
                        exception -> {
                            assertThat(exception.getCode()).isEqualTo(
                                    "INVALID_OR_EXPIRED_EMAIL_VERIFICATION");

                            assertThat(exception.getField())
                                    .isEqualTo("token");

                            assertThat(exception.getMessage())
                                    .doesNotContain(RAW_TOKEN)
                                    .doesNotContain(TOKEN_HASH);
                        });
    }
}