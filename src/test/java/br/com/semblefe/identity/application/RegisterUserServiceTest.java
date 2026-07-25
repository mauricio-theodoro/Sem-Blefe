package br.com.semblefe.identity.application;

import br.com.semblefe.identity.application.model.NewUser;
import br.com.semblefe.identity.application.model.RegisterUserCommand;
import br.com.semblefe.identity.application.port.outbound.CurrentLegalDocuments;
import br.com.semblefe.identity.application.port.outbound.PasswordHasher;
import br.com.semblefe.identity.application.port.outbound.UserRegistrationPersistence;
import br.com.semblefe.shared.domain.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    private UserRegistrationPersistence registrationPersistence;
    private PasswordHasher passwordHasher;
    private CurrentLegalDocuments currentLegalDocuments;
    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        registrationPersistence = mock(UserRegistrationPersistence.class);
        passwordHasher = mock(PasswordHasher.class);
        currentLegalDocuments = mock(CurrentLegalDocuments.class);
        service = new RegisterUserService(
                registrationPersistence,
                passwordHasher,
                currentLegalDocuments);
    }

    @Test
    void shouldPreparePendingRegistrationWithBackendControlledData() {
        String password = "Minha frase secreta 2026!";
        when(passwordHasher.hash(password)).thenReturn("{argon2}hash-seguro");
        when(currentLegalDocuments.termsOfUseVersion()).thenReturn("1.0");
        when(currentLegalDocuments.privacyPolicyVersion()).thenReturn("1.0");
        when(registrationPersistence.register(any())).thenReturn(true);

        service.execute(new RegisterUserCommand(
                "  Mauricio@Exemplo.COM  ",
                password,
                "1.0",
                "1.0",
                true,
                true));

        ArgumentCaptor<NewUser> captor =
                ArgumentCaptor.forClass(NewUser.class);
        verify(registrationPersistence).register(captor.capture());

        NewUser user = captor.getValue();
        assertThat(user.email().value()).isEqualTo("Mauricio@Exemplo.COM");
        assertThat(user.email().normalized()).isEqualTo("mauricio@exemplo.com");
        assertThat(user.passwordHash()).isEqualTo("{argon2}hash-seguro");
        assertThat(user.initialStatus()).isEqualTo("PENDENTE_EMAIL");
        assertThat(user.emailVerified()).isFalse();
        assertThat(user.initialRole()).isEqualTo("USUARIO");
        assertThat(user.legalAcceptanceSource()).isEqualTo("PWA");
        assertThat(user.termsOfUseVersion()).isEqualTo("1.0");
        assertThat(user.privacyPolicyVersion()).isEqualTo("1.0");
    }

    @Test
    void shouldCalculateHashEvenWhenPersistenceDetectsDuplicate() {
        String password = "Outra frase secreta segura!";
        when(passwordHasher.hash(password)).thenReturn("{argon2}hash");
        when(currentLegalDocuments.termsOfUseVersion()).thenReturn("1.0");
        when(currentLegalDocuments.privacyPolicyVersion()).thenReturn("1.0");
        when(registrationPersistence.register(any())).thenReturn(false);

        service.execute(new RegisterUserCommand(
                "existente@exemplo.com",
                password,
                "1.0",
                "1.0",
                true,
                true));

        verify(passwordHasher).hash(password);
        verify(registrationPersistence).register(any());
    }

    @Test
    void shouldRejectOutdatedLegalDocumentBeforeHashing() {
        when(currentLegalDocuments.termsOfUseVersion()).thenReturn("1.1");
        when(currentLegalDocuments.privacyPolicyVersion()).thenReturn("1.0");

        assertThatThrownBy(() -> service.execute(new RegisterUserCommand(
                "usuario@exemplo.com",
                "Minha frase secreta segura!",
                "1.0",
                "1.0",
                true,
                true)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("documento foi atualizado");

        verify(passwordHasher, never()).hash(anyString());
        verify(registrationPersistence, never()).register(any());
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
        verify(registrationPersistence, never()).register(any());
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
        verify(registrationPersistence, never()).register(any());
    }
}
