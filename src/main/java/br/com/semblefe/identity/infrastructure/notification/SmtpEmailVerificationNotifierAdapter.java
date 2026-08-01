package br.com.semblefe.identity.infrastructure.notification;

import br.com.semblefe.identity.application.port.outbound.EmailVerificationNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;

@Component
public class SmtpEmailVerificationNotifierAdapter
        implements EmailVerificationNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            SmtpEmailVerificationNotifierAdapter.class);

    private final JavaMailSender mailSender;
    private final String sender;
    private final String confirmationPageUrl;

    public SmtpEmailVerificationNotifierAdapter(
            JavaMailSender mailSender,
            @Value("${semblefe.identity.email-verification.sender}")
            String sender,
            @Value("${semblefe.identity.email-verification.confirmation-page-url}")
            String confirmationPageUrl) {

        this.mailSender = mailSender;
        this.sender = requireText(
                sender,
                "O remetente de confirmação não foi configurado.");
        this.confirmationPageUrl = validateConfirmationPageUrl(
                confirmationPageUrl);
    }

    @Override
    @Async("emailNotificationExecutor")
    public void send(
            String destinationEmail,
            String rawToken,
            Instant expiresAt) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(destinationEmail);
        message.setSubject("Confirme seu e-mail — Sem Blefe");
        message.setText("""
                Olá!

                Confirme seu endereço de e-mail para ativar sua conta Sem Blefe:

                %s#token=%s

                Este link é de uso único e expira em %s.
                Se você não solicitou este cadastro, ignore esta mensagem.
                """.formatted(
                confirmationPageUrl,
                rawToken,
                expiresAt));

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            // Token and destination are deliberately omitted from logs.
            LOGGER.error(
                    "Não foi possível entregar o e-mail de confirmação. Tipo da falha: {}",
                    exception.getClass().getSimpleName());
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }

        return value.strip();
    }

    private String validateConfirmationPageUrl(String value) {
        String url = requireText(
                value,
                "A página de confirmação não foi configurada.");

        try {
            URI uri = URI.create(url);
            boolean validScheme =
                    "http".equalsIgnoreCase(uri.getScheme())
                            || "https".equalsIgnoreCase(uri.getScheme());

            if (!validScheme
                    || uri.getHost() == null
                    || uri.getUserInfo() != null
                    || uri.getFragment() != null) {
                throw invalidConfirmationPageUrl();
            }

            return uri.toString();
        } catch (IllegalArgumentException exception) {
            throw invalidConfirmationPageUrl();
        }
    }

    private IllegalStateException invalidConfirmationPageUrl() {
        return new IllegalStateException(
                "A página de confirmação deve ser uma URL HTTP(S) válida e sem fragmento.");
    }
}