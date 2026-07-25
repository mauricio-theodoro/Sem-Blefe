package br.com.semblefe.identity.application;

import br.com.semblefe.identity.application.model.NewUser;
import br.com.semblefe.identity.application.model.RegisterUserCommand;
import br.com.semblefe.identity.application.port.inbound.RegisterUserUseCase;
import br.com.semblefe.identity.application.port.outbound.CurrentLegalDocuments;
import br.com.semblefe.identity.application.port.outbound.PasswordHasher;
import br.com.semblefe.identity.application.port.outbound.UserRegistrationPersistence;
import br.com.semblefe.identity.domain.Email;
import br.com.semblefe.identity.domain.PasswordPolicy;
import br.com.semblefe.shared.domain.BusinessValidationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRegistrationPersistence registrationPersistence;
    private final PasswordHasher passwordHasher;
    private final CurrentLegalDocuments currentLegalDocuments;

    public RegisterUserService(
            UserRegistrationPersistence registrationPersistence,
            PasswordHasher passwordHasher,
            CurrentLegalDocuments currentLegalDocuments) {
        this.registrationPersistence = registrationPersistence;
        this.passwordHasher = passwordHasher;
        this.currentLegalDocuments = currentLegalDocuments;
    }

    @Override
    public void execute(RegisterUserCommand command) {
        PasswordPolicy.validate(command.password());
        Email email = Email.of(command.email());
        String termsOfUseVersion =
                currentLegalDocuments.termsOfUseVersion();
        String privacyPolicyVersion =
                currentLegalDocuments.privacyPolicyVersion();

        validateDocumentVersion(
                command.termsOfUseVersion(),
                termsOfUseVersion,
                "termsOfUseVersion");
        validateDocumentVersion(
                command.privacyPolicyVersion(),
                privacyPolicyVersion,
                "privacyPolicyVersion");

        // Hashing is performed even for duplicate attempts to reduce timing
        // differences that could otherwise facilitate account enumeration.
        String passwordHash = passwordHasher.hash(command.password());

        NewUser user = new NewUser(
                UUID.randomUUID(),
                email,
                passwordHash,
                termsOfUseVersion,
                privacyPolicyVersion);

        // The API response is neutral; the persistence result is not exposed.
        registrationPersistence.register(user);
    }

    private void validateDocumentVersion(
            String providedVersion,
            String currentVersion,
            String field) {
        if (!currentVersion.equals(providedVersion)) {
            throw new BusinessValidationException(
                    "OUTDATED_LEGAL_DOCUMENT",
                    field,
                    "O documento foi atualizado. Recarregue o conteúdo antes de aceitar.");
        }
    }
}
