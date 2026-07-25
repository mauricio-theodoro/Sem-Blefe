package br.com.semblefe.identity.infrastructure.security;

import br.com.semblefe.identity.application.port.outbound.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2PasswordHasherAdapter implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public Argon2PasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String password) {
        return passwordEncoder.encode(password);
    }
}
