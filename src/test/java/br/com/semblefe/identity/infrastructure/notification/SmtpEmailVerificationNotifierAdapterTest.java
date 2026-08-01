package br.com.semblefe.identity.infrastructure.notification;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpEmailVerificationNotifierAdapterTest {

    private static final String RAW_TOKEN = "a".repeat(43);

    @Test
    void shouldSendTokenInUrlFragment() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        SmtpEmailVerificationNotifierAdapter notifier =
                new SmtpEmailVerificationNotifierAdapter(
                        mailSender,
                        "no-reply@semblefe.local",
                        "http://localhost:5173/confirm-email");

        notifier.send(
                "artista@exemplo.com",
                RAW_TOKEN,
                Instant.parse("2026-08-01T12:30:00Z"));

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertThat(message.getFrom())
                .isEqualTo("no-reply@semblefe.local");
        assertThat(message.getTo())
                .containsExactly("artista@exemplo.com");
        assertThat(message.getSubject())
                .contains("Sem Blefe");
        assertThat(message.getText()).contains(
                "http://localhost:5173/confirm-email#token=" + RAW_TOKEN);
    }

    @Test
    void shouldKeepApplicationAvailableWhenSmtpIsUnavailable() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        doThrow(new MailSendException("SMTP indisponível"))
                .when(mailSender)
                .send(org.mockito.ArgumentMatchers.any(
                        SimpleMailMessage.class));

        SmtpEmailVerificationNotifierAdapter notifier =
                new SmtpEmailVerificationNotifierAdapter(
                        mailSender,
                        "no-reply@semblefe.local",
                        "http://localhost:5173/confirm-email");

        assertThatCode(() -> notifier.send(
                "artista@exemplo.com",
                RAW_TOKEN,
                Instant.parse("2026-08-01T12:30:00Z")))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectConfirmationPageWithExistingFragment() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        assertThatThrownBy(() ->
                new SmtpEmailVerificationNotifierAdapter(
                        mailSender,
                        "no-reply@semblefe.local",
                        "http://localhost:5173/confirm-email#unsafe"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectConfirmationPageWithoutValidHost() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        assertThatThrownBy(() ->
                new SmtpEmailVerificationNotifierAdapter(
                        mailSender,
                        "no-reply@semblefe.local",
                        "https:///confirm-email"))
                .isInstanceOf(IllegalStateException.class);
    }
}