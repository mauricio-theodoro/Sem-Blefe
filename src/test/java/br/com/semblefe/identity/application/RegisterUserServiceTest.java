package br.com.semblefe.identity.application;

import br.com.semblefe.identity.application.model.EmailVerificationDelivery;
import br.com.semblefe.identity.application.model.NewEmailVerification;
import br.com.semblefe.identity.application.model.NewUser;
import br.com.semblefe.identity.application.model.RegisterUserCommand;
import br.com.semblefe.identity.application.port.outbound.CurrentLegalDocuments;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationNotifier;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationSettings;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationTokenSecurity;
import br.com.semblefe.identity.application.port.outbound.PasswordHasher;
import br.com.semblefe.identity.application.port.outbound.UserRegistrationPersistence;
import br.com.semblefe.shared.domain.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    private static final Instant DATABASE_EXPIRES_AT =
            Instant.parse("2026-08-01T12:30:00Z");

    private static final String RAW_TOKEN = "a".repeat(43);
    private static final String TOKEN_HASH = "b".repeat(64);

    private UserRegistrationPersistence registrationPersistence;
    private PasswordHasher passwordHasher;
    private CurrentLegalDocuments currentLegalDocuments;
    private EmailVerificationTokenSecurity tokenSecurity;
    private EmailVerificationSettings verificationSettings;
    private EmailVerificationNotifier verificationNotifier;
    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        registrationPersistence =
                mock(UserRegistrationPersistence.class);
        passwordHasher = mock(PasswordHasher.class);
        currentLegalDocuments =
                mock(CurrentLegalDocuments.class);
        tokenSecurity =
                mock(EmailVerificationTokenSecurity.class);
        verificationSettings =
                mock(EmailVerificationSettings.class);
        verificationNotifier =
                mock(EmailVerificationNotifier.class);

        service = new RegisterUserService(
                registrationPersistence,
                passwordHasher,
                currentLegalDocuments,
                tokenSecurity,
                verificationSettings,
                verificationNotifier);

        when(tokenSecurity.generate()).thenReturn(RAW_TOKEN);
        when(tokenSecurity.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        when(verificationSettings.tokenValidity())
                .thenReturn(Duration.ofMinutes(30));
    }

    @Test
    void shouldPreparePendingRegistrationWithBackendControlledData() {
        String password = "Minha frase secreta 2026!";

        when(passwordHasher.hash(password))
                .thenReturn("{argon2}hash-seguro");

        when(currentLegalDocuments.termsOfUseVersion())
                .thenReturn("1.0");

        when(currentLegalDocuments.privacyPolicyVersion())
                .thenReturn("1.0");

        when(registrationPersistence.register(any(), any()))
                .thenReturn(Optional.of(
                        new EmailVerificationDelivery(
                                "Mauricio@Exemplo.COM",
                                DATABASE_EXPIRES_AT)));

        service.execute(new RegisterUserCommand(
                "  Mauricio@Exemplo.COM  ",
                password,
                "1.0",
                "1.0",
                true,
                true));

        ArgumentCaptor<NewUser> userCaptor =
                ArgumentCaptor.forClass(NewUser.class);

        ArgumentCaptor<NewEmailVerification> verificationCaptor =
                ArgumentCaptor.forClass(
                        NewEmailVerification.class);

        verify(registrationPersistence).register(
                userCaptor.capture(),
                verificationCaptor.capture());

        NewUser user = userCaptor.getValue();

        assertThat(user.email().value())
                .isEqualTo("Mauricio@Exemplo.COM");
        assertThat(user.email().normalized())
                .isEqualTo("mauricio@exemplo.com");
        assertThat(user.passwordHash())
                .isEqualTo("{argon2}hash-seguro");
        assertThat(user.initialStatus())
                .isEqualTo("PENDENTE_EMAIL");
        assertThat(user.emailVerified()).isFalse();
        assertThat(user.initialRole()).isEqualTo("USUARIO");
        assertThat(user.legalAcceptanceSource()).isEqualTo("PWA");
        assertThat(user.termsOfUseVersion()).isEqualTo("1.0");
        assertThat(user.privacyPolicyVersion()).isEqualTo("1.0");

        NewEmailVerification verification =
                verificationCaptor.getValue();

        assertThat(verification.userId()).isEqualTo(user.id());
        assertThat(verification.tokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(verification.tokenValidity())
                .isEqualTo(Duration.ofMinutes(30));

        verify(verificationNotifier).send(
                "Mauricio@Exemplo.COM",
                RAW_TOKEN,
                DATABASE_EXPIRES_AT);
    }

    @Test
    void shouldCalculateHashEvenWhenPersistenceDetectsDuplicate() {
        String password = "Outra frase secreta segura!";

        when(passwordHasher.hash(password))
                .thenReturn("{argon2}hash");

        when(currentLegalDocuments.termsOfUseVersion())
                .thenReturn("1.0");

        when(currentLegalDocuments.privacyPolicyVersion())
                .thenReturn("1.0");

        when(registrationPersistence.register(any(), any()))
                .thenReturn(Optional.empty());

        service.execute(new RegisterUserCommand(
                "existente@exemplo.com",
                password,
                "1.0",
                "1.0",
                true,
                true));

        verify(passwordHasher).hash(password);
        verify(tokenSecurity).generate();
        verify(tokenSecurity).hash(RAW_TOKEN);
        verify(registrationPersistence).register(any(), any());

        verify(verificationNotifier, never()).send(
                anyString(),
                anyString(),
                any());
    }

    @Test
    void shouldRejectOutdatedLegalDocumentBeforeHashing() {
        when(currentLegalDocuments.termsOfUseVersion())
                .thenReturn("1.1");

        when(currentLegalDocuments.privacyPolicyVersion())
                .thenReturn("1.0");

        assertThatThrownBy(() ->
                service.execute(new RegisterUserCommand(
                        "usuario@exemplo.com",
                        "Minha frase secreta segura!",
                        "1.0",
                        "1.0",
                        true,
                        true)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("documento foi atualizado");

        verify(passwordHasher, never()).hash(anyString());
        verify(registrationPersistence, never())
                .register(any(), any());
    }

    @Test
    void shouldRejectMissingTermsConsentBeforeHashing() {
        assertThatThrownBy(() -> new RegisterUserCommand(
                "usuario@exemplo.com",
                "Minha frase secreta segura!",
                "1.0",
                "1.0",
                false,
                true))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Termos de Uso");

        verify(passwordHasher, never()).hash(anyString());
        verify(registrationPersistence, never())
                .register(any(), any());
    }

    @Test
    void shouldRejectMissingPrivacyConsentBeforeHashing() {
        assertThatThrownBy(() -> new RegisterUserCommand(
                "usuario@exemplo.com",
                "Minha frase secreta segura!",
                "1.0",
                "1.0",
                true,
                false))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Política de Privacidade");

        verify(passwordHasher, never()).hash(anyString());
        verify(registrationPersistence, never())
                .register(any(), any());
    }
}