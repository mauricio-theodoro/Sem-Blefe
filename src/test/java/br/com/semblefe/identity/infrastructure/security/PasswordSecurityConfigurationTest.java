package br.com.semblefe.identity.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordSecurityConfigurationTest {

    @Test
    void shouldGenerateArgon2idHashWithPrefixAndVerifyPassword() {
        PasswordEncoder passwordEncoder =
                new PasswordSecurityConfiguration().passwordEncoder();
        String password = "Minha frase musical segura 2026!";

        String hash = passwordEncoder.encode(password);
        String secondHash = passwordEncoder.encode(password);

        assertThat(hash)
                .startsWith("{argon2}$argon2id$")
                .doesNotContain(password);
        assertThat(secondHash).isNotEqualTo(hash);
        assertThat(passwordEncoder.matches(password, hash)).isTrue();
        assertThat(passwordEncoder.matches("senha diferente", hash)).isFalse();
    }
}
