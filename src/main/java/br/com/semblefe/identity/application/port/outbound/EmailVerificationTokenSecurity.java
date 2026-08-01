package br.com.semblefe.identity.application.port.outbound;

/**
 * Cryptographic operations for e-mail verification tokens.
 */
public interface EmailVerificationTokenSecurity {

    String generate();

    String hash(String rawToken);
}