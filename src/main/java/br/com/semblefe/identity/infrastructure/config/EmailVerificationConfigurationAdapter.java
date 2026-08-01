package br.com.semblefe.identity.infrastructure.config;

import br.com.semblefe.identity.application.port.outbound.EmailVerificationSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class EmailVerificationConfigurationAdapter
        implements EmailVerificationSettings {

    private static final Duration MINIMUM_DURATION = Duration.ofSeconds(1);
    private static final Duration MAXIMUM_VALIDITY = Duration.ofHours(24);
    private static final Duration MAXIMUM_COOLDOWN = Duration.ofHours(1);

    private final Duration tokenValidity;
    private final Duration resendCooldown;

    public EmailVerificationConfigurationAdapter(
            @Value("${semblefe.identity.email-verification.token-validity}")
            Duration tokenValidity,
            @Value("${semblefe.identity.email-verification.resend-cooldown}")
            Duration resendCooldown) {

        if (tokenValidity == null
                || tokenValidity.compareTo(MINIMUM_DURATION) < 0
                || tokenValidity.compareTo(MAXIMUM_VALIDITY) > 0) {
            throw new IllegalStateException(
                    "A validade do token deve ser de 1 segundo a 24 horas.");
        }

        this.tokenValidity = tokenValidity;

        if (resendCooldown == null
                || resendCooldown.compareTo(MINIMUM_DURATION) < 0
                || resendCooldown.compareTo(MAXIMUM_COOLDOWN) > 0
                || resendCooldown.compareTo(tokenValidity) >= 0) {
            throw new IllegalStateException(
                    "O intervalo de reenvio deve ser de pelo menos 1 segundo, menor que a validade do token e de no máximo 1 hora.");
        }

        this.resendCooldown = resendCooldown;
    }

    @Override
    public Duration tokenValidity() {
        return tokenValidity;
    }

    @Override
    public Duration resendCooldown() {
        return resendCooldown;
    }
}