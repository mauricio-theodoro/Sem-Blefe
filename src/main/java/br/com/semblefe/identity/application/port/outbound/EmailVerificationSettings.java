package br.com.semblefe.identity.application.port.outbound;

import java.time.Duration;

public interface EmailVerificationSettings {

    Duration tokenValidity();

    Duration resendCooldown();
}