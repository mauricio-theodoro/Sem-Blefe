package br.com.semblefe.identity.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class SecureEmailVerificationTokenAdapterTest {

    private final SecureEmailVerificationTokenAdapter tokenSecurity =
            new SecureEmailVerificationTokenAdapter();

    @Test
    void shouldGenerateTwoHundredFiftySixBitUrlSafeTokenWithoutPadding() {
        String token = tokenSecurity.generate();

        assertThat(token).matches("[A-Za-z0-9_-]{43}");
        assertThat(token).doesNotContain("=");
        assertThat(Base64.getUrlDecoder().decode(token)).hasSize(32);
    }

    @Test
    void shouldGenerateDistinctTokens() {
        assertThat(tokenSecurity.generate())
                .isNotEqualTo(tokenSecurity.generate());
    }

    @Test
    void shouldHashWithSha256AsLowercaseHexadecimal() {
        assertThat(tokenSecurity.hash("abc"))
                .isEqualTo(
                        "ba7816bf8f01cfea414140de5dae2223"
                                + "b00361a396177a9cb410ff61f20015ad");
    }
}