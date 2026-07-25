package br.com.semblefe.identity.api.validation;

import br.com.semblefe.identity.domain.PasswordPolicy;
import br.com.semblefe.shared.domain.BusinessValidationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context) {
        try {
            PasswordPolicy.validate(password);
            return true;
        } catch (BusinessValidationException exception) {
            return false;
        }
    }
}
