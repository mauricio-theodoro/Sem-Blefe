package br.com.semblefe.identity.infrastructure.security;

import br.com.semblefe.identity.application.port.outbound.EmailVerificationTokenSecurity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class SecureEmailVerificationTokenAdapter
        implements EmailVerificationTokenSecurity {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom;

    public SecureEmailVerificationTokenAdapter() {
        this(new SecureRandom());
    }

    SecureEmailVerificationTokenAdapter(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Override
    public String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    @Override
    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "O token para cálculo do hash é obrigatório.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] tokenHash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(tokenHash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 não está disponível nesta JVM.",
                    exception);
        }
    }
}