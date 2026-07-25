package br.com.semblefe.identity.api;

import br.com.semblefe.identity.application.model.RegisterUserCommand;
import br.com.semblefe.identity.application.port.inbound.RegisterUserUseCase;
import br.com.semblefe.shared.config.JacksonConfiguration;
import br.com.semblefe.shared.config.SecurityConfiguration;
import br.com.semblefe.shared.domain.BusinessValidationException;
import br.com.semblefe.shared.web.GlobalExceptionHandler;
import br.com.semblefe.shared.web.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRegistrationController.class)
@Import({
        JacksonConfiguration.class,
        SecurityConfiguration.class,
        RequestIdFilter.class,
        GlobalExceptionHandler.class
})
class UserRegistrationControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @Test
    void shouldAcceptValidRegistrationWithoutExposingInternalData()
            throws Exception {
        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", "registration-web-001")
                        .content("""
                                {
                                  "email": "  usuario@exemplo.com  ",
                                  "password": "  Minha frase secreta 2026!  ",
                                  "termsOfUseVersion": "1.0",
                                  "privacyPolicyVersion": "1.0",
                                  "acceptedTermsOfUse": true,
                                  "acceptedPrivacyPolicy": true
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(header().string(
                        "X-Request-Id",
                        "registration-web-001"))
                .andExpect(jsonPath("$.code").value("REGISTRATION_RECEIVED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        ArgumentCaptor<RegisterUserCommand> captor =
                ArgumentCaptor.forClass(RegisterUserCommand.class);
        verify(registerUserUseCase).execute(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("usuario@exemplo.com");
        assertThat(captor.getValue().password())
                .isEqualTo("  Minha frase secreta 2026!  ");
    }

    @Test
    void shouldRejectUnknownPrivilegedFields() throws Exception {
        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "usuario@exemplo.com",
                                  "password": "Minha frase secreta 2026!",
                                  "termsOfUseVersion": "1.0",
                                  "privacyPolicyVersion": "1.0",
                                  "acceptedTermsOfUse": true,
                                  "acceptedPrivacyPolicy": true,
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        "O corpo da requisição contém campos ou valores inválidos."));

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void shouldRejectInvalidEmailPasswordAndConsents() throws Exception {
        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "email-invalido",
                                  "password": "curta",
                                  "termsOfUseVersion": "1.0",
                                  "privacyPolicyVersion": "1.0",
                                  "acceptedTermsOfUse": false,
                                  "acceptedPrivacyPolicy": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath(
                        "$.fieldViolations[*].field",
                        hasItems(
                                "email",
                                "password",
                                "acceptedTermsOfUse",
                                "acceptedPrivacyPolicy")));

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void shouldCountUnicodePasswordCharactersByCodePoint() throws Exception {
        String unicodePassword = "🎵🎶".repeat(64);

        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unicode@exemplo.com",
                                  "password": "%s",
                                  "termsOfUseVersion": "1.0",
                                  "privacyPolicyVersion": "1.0",
                                  "acceptedTermsOfUse": true,
                                  "acceptedPrivacyPolicy": true
                                }
                                """.formatted(unicodePassword)))
                .andExpect(status().isAccepted());

        verify(registerUserUseCase).execute(any());
    }

    @Test
    void shouldRejectPasswordWithMoreThanOneHundredTwentyEightCodePoints()
            throws Exception {
        String unicodePassword = "🎵🎶".repeat(64) + "🎤";

        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unicode@exemplo.com",
                                  "password": "%s",
                                  "termsOfUseVersion": "1.0",
                                  "privacyPolicyVersion": "1.0",
                                  "acceptedTermsOfUse": true,
                                  "acceptedPrivacyPolicy": true
                                }
                                """.formatted(unicodePassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldViolations[0].field")
                        .value("password"));

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void shouldRequestReloadWhenLegalDocumentIsOutdated() throws Exception {
        doThrow(new BusinessValidationException(
                "OUTDATED_LEGAL_DOCUMENT",
                "termsOfUseVersion",
                "O documento foi atualizado. Recarregue o conteúdo antes de aceitar."))
                .when(registerUserUseCase)
                .execute(any());

        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "usuario@exemplo.com",
                                  "password": "Minha frase secreta 2026!",
                                  "termsOfUseVersion": "1.0",
                                  "privacyPolicyVersion": "1.0",
                                  "acceptedTermsOfUse": true,
                                  "acceptedPrivacyPolicy": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("OUTDATED_LEGAL_DOCUMENT"))
                .andExpect(jsonPath("$.fieldViolations[0].field")
                        .value("termsOfUseVersion"));
    }

    @Test
    void shouldContinueDenyingOtherPublicPostRoutes() throws Exception {
        mockMvc.perform(post("/api/v1/public/unreleased-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
