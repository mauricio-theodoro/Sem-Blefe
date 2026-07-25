package br.com.semblefe.identity.domain;

import br.com.semblefe.shared.domain.BusinessValidationException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Initial password policy.
 *
 * <p>The original password is never transformed before hashing. Normalization
 * is used only to identify obviously predictable values.</p>
 */
public final class PasswordPolicy {

    public static final int MINIMUM_LENGTH = 15;
    public static final int MAXIMUM_LENGTH = 128;

    private static final Set<String> PREDICTABLE_PASSWORDS = Set.of(
            "123456789012345",
            "abcdefghijklmnop",
            "administrador123",
            "passwordpassword",
            "qwertyuiopasdfg",
            "semblefe1234567",
            "senha1234567890");

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.isBlank()) {
            throw invalidPassword("Informe uma senha.");
        }

        int length = password.codePointCount(0, password.length());

        if (length < MINIMUM_LENGTH || length > MAXIMUM_LENGTH) {
            throw invalidPassword(
                    "A senha deve possuir entre %d e %d caracteres."
                            .formatted(MINIMUM_LENGTH, MAXIMUM_LENGTH));
        }

        String comparisonValue = Normalizer.normalize(
                        password,
                        Normalizer.Form.NFKC)
                .strip()
                .toLowerCase(Locale.ROOT);

        if (PREDICTABLE_PASSWORDS.contains(comparisonValue)
                || comparisonValue.codePoints().distinct().count() == 1) {
            throw invalidPassword("Escolha uma senha menos previsível.");
        }
    }

    private static BusinessValidationException invalidPassword(String message) {
        return new BusinessValidationException(
                "INVALID_PASSWORD",
                "password",
                message);
    }
}
