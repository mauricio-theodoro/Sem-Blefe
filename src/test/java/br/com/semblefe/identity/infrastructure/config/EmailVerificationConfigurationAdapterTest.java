package br.com.semblefe.identity.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailVerificationConfigurationAdapterTest {

    @Test
    void shouldAcceptPositiveValidityUpToTwentyFourHours() {
        EmailVerificationConfigurationAdapter configuration =
                new EmailVerificationConfigurationAdapter(
                        Duration.ofMinutes(30),
                        Duration.ofMinutes(1));

        assertThat(configuration.tokenValidity())
                .isEqualTo(Duration.ofMinutes(30));
        assertThat(configuration.resendCooldown())
                .isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldRejectInvalidValidity() {
        assertThatThrownBy(() ->
                new EmailVerificationConfigurationAdapter(
                        Duration.ofNanos(1),
                        Duration.ofSeconds(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() ->
                new EmailVerificationConfigurationAdapter(
                        Duration.ZERO,
                        Duration.ofMinutes(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() ->
                new EmailVerificationConfigurationAdapter(
                        Duration.ofHours(25),
                        Duration.ofMinutes(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() ->
                new EmailVerificationConfigurationAdapter(
                        Duration.ofMinutes(30),
                        Duration.ofMinutes(30)))
                .isInstanceOf(IllegalStateException.class);
    }
}