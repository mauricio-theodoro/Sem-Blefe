package br.com.semblefe.identity.domain;

import br.com.semblefe.shared.domain.BusinessValidationException;

import java.util.Locale;

/**
 * Email address preserved for communication and normalized for identity checks.
 */
public record Email(String value, String normalized) {

    public Email {
        if (!hasValidBasicFormat(value)
                || normalized == null
                || normalized.length() > 254
                || !normalized.equals(value.toLowerCase(Locale.ROOT))) {
            throw invalidEmail();
        }
    }

    public static Email of(String receivedValue) {
        if (receivedValue == null) {
            throw invalidEmail();
        }

        String value = receivedValue.strip();
        return new Email(value, value.toLowerCase(Locale.ROOT));
    }

    private static boolean hasValidBasicFormat(String value) {
        if (value == null || !value.equals(value.strip())) {
            return false;
        }

        int separator = value.lastIndexOf('@');
        if (value.length() < 3 || value.length() > 254) {
            return false;
        }

        return separator > 0
                && separator < value.length() - 1
                && value.indexOf('@') == separator
                && value.codePoints().noneMatch(Email::isForbiddenCharacter);
    }

    private static boolean isForbiddenCharacter(int character) {
        return Character.isWhitespace(character)
                || Character.isISOControl(character);
    }

    private static BusinessValidationException invalidEmail() {
        return new BusinessValidationException(
                "INVALID_EMAIL",
                "email",
                "Informe um e-mail válido.");
    }
}
