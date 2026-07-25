package br.com.semblefe.identity.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Configuration
public class PasswordSecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        PasswordEncoder argon2id = new Argon2PasswordEncoder(
                16,
                32,
                1,
                19_456,
                2);

        return new DelegatingPasswordEncoder(
                "argon2",
                Map.of("argon2", argon2id));
    }
}
