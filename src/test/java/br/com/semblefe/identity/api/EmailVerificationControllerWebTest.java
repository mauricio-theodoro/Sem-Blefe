package br.com.semblefe.identity.api;

import br.com.semblefe.identity.application.model.VerifyEmailCommand;
import br.com.semblefe.identity.application.port.inbound.VerifyEmailUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailVerificationController.class)
@Import({
        JacksonConfiguration.class,
        SecurityConfiguration.class,
        RequestIdFilter.class,
        GlobalExceptionHandler.class
})
class EmailVerificationControllerWebTest {

    private static final String RAW_TOKEN = "a".repeat(43);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerifyEmailUseCase verifyEmailUseCase;

    @Test
    void shouldVerifyEmailWithoutExposingSensitiveData()
            throws Exception {

        mockMvc.perform(post(
                        "/api/v1/public/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "X-Request-Id",
                                "verification-web-001")
                        .content("""
                                {"token":"%s"}
                                """.formatted(RAW_TOKEN)))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "X-Request-Id",
                        "verification-web-001"))
                .andExpect(jsonPath("$.code")
                        .value("EMAIL_VERIFIED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.tokenHash").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());

        ArgumentCaptor<VerifyEmailCommand> captor =
                ArgumentCaptor.forClass(
                        VerifyEmailCommand.class);

        verify(verifyEmailUseCase).execute(captor.capture());

        assertThat(captor.getValue().token())
                .isEqualTo(RAW_TOKEN);

        assertThat(captor.getValue().requestId())
                .isEqualTo("verification-web-001");

        assertThat(captor.getValue().toString())
                .doesNotContain(RAW_TOKEN);
    }

    @Test
    void shouldRejectMalformedTokenBeforeUseCase()
            throws Exception {

        mockMvc.perform(post(
                        "/api/v1/public/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"token-invalido"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST"))
                .andExpect(jsonPath(
                        "$.fieldViolations[0].field")
                        .value("token"));

        verifyNoInteractions(verifyEmailUseCase);
    }

    @Test
    void shouldRejectUnknownFields() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/public/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token":"%s",
                                  "role":"ADMIN"
                                }
                                """.formatted(RAW_TOKEN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_REQUEST"));

        verifyNoInteractions(verifyEmailUseCase);
    }

    @Test
    void shouldReturnNeutralErrorForInvalidExpiredOrUsedToken()
            throws Exception {

        doThrow(new BusinessValidationException(
                "INVALID_OR_EXPIRED_EMAIL_VERIFICATION",
                "token",
                "O link de confirmação é inválido ou expirou."))
                .when(verifyEmailUseCase)
                .execute(any());

        mockMvc.perform(post(
                        "/api/v1/public/email-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(RAW_TOKEN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(
                        "INVALID_OR_EXPIRED_EMAIL_VERIFICATION"))
                .andExpect(jsonPath(
                        "$.fieldViolations[0].field")
                        .value("token"))
                .andExpect(jsonPath(
                        "$.fieldViolations[0].message")
                        .value(
                                "O link de confirmação é inválido ou expirou."))
                .andExpect(jsonPath("$.token").doesNotExist());
    }
}