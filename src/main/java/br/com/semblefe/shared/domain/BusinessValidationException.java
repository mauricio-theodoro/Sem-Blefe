package br.com.semblefe.shared.domain;

/**
 * Validation failure detected beyond the HTTP layer boundary.
 *
 * <p>The field is reported without carrying the rejected value, preventing
 * sensitive data from being copied to responses or logs.</p>
 */
public class BusinessValidationException extends RuntimeException {

    private final String code;
    private final String field;

    public BusinessValidationException(String code, String field, String message) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }
}
