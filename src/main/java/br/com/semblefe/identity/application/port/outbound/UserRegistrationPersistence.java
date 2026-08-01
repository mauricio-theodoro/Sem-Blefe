package br.com.semblefe.identity.application.port.outbound;

import br.com.semblefe.identity.application.model.EmailVerificationDelivery;
import br.com.semblefe.identity.application.model.NewEmailVerification;
import br.com.semblefe.identity.application.model.NewUser;

import java.util.Optional;

public interface UserRegistrationPersistence {

    /**
     * @return delivery data when the account and its verification token are
     * created, or empty when the normalized email address already exists.
     */
    Optional<EmailVerificationDelivery> register(
            NewUser user,
            NewEmailVerification emailVerification);
}