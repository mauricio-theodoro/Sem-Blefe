package br.com.semblefe.identity.application.port.outbound;

import br.com.semblefe.identity.application.model.NewUser;

public interface UserRegistrationPersistence {

    /**
     * @return {@code true} when the account is created or {@code false} when
     * the normalized email address is already registered.
     */
    boolean register(NewUser user);
}
