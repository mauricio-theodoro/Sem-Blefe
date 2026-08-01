package br.com.semblefe.identity.application;

import br.com.semblefe.identity.application.model.EmailVerificationDelivery;
import br.com.semblefe.identity.application.model.NewEmailVerification;
import br.com.semblefe.identity.application.model.NewUser;
import br.com.semblefe.identity.application.model.RegisterUserCommand;
import br.com.semblefe.identity.application.port.inbound.RegisterUserUseCase;
import br.com.semblefe.identity.application.port.outbound.CurrentLegalDocuments;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationNotifier;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationSettings;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationTokenSecurity;
import br.com.semblefe.identity.application.port.outbound.PasswordHasher;
import br.com.semblefe.identity.application.port.outbound.UserRegistrationPersistence;
import br.com.semblefe.identity.domain.Email;
import br.com.semblefe.identity.domain.PasswordPolicy;
import br.com.semblefe.shared.domain.BusinessValidationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRegistrationPersistence registrationPersistence;
    private final PasswordHasher passwordHasher;
    private final CurrentLegalDocuments currentLegalDocuments;
    private final EmailVerificationTokenSecurity tokenSecurity;
    private final EmailVerificationSettings verificationSettings;
    private final EmailVerificationNotifier verificationNotifier;

    public RegisterUserService(
            UserRegistrationPersistence registrationPersistence,
            PasswordHasher passwordHasher,
            CurrentLegalDocuments currentLegalDocuments,
            EmailVerificationTokenSecurity tokenSecurity,
            EmailVerificationSettings verificationSettings,
            EmailVerificationNotifier verificationNotifier) {

        this.registrationPersistence = registrationPersistence;
        this.passwordHasher = passwordHasher;
        this.currentLegalDocuments = currentLegalDocuments;
        this.tokenSecurity = tokenSecurity;
        this.verificationSettings = verificationSettings;
        this.verificationNotifier = verificationNotifier;
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

        /*
         * O hash da senha é calculado inclusive nas tentativas duplicadas.
         * Isso reduz diferenças de tempo que poderiam ajudar na enumeração
         * de contas.
         */
        String passwordHash = passwordHasher.hash(command.password());

        /*
         * A geração também acontece antes de sabermos se o e-mail existe,
         * mantendo um caminho de execução mais uniforme.
         */
        String rawVerificationToken = tokenSecurity.generate();

        String verificationTokenHash =
                tokenSecurity.hash(rawVerificationToken);

        NewUser user = new NewUser(
                UUID.randomUUID(),
                email,
                passwordHash,
                termsOfUseVersion,
                privacyPolicyVersion);

        NewEmailVerification emailVerification =
                new NewEmailVerification(
                        UUID.randomUUID(),
                        user.id(),
                        verificationTokenHash,
                        verificationSettings.tokenValidity());

        Optional<EmailVerificationDelivery> delivery =
                registrationPersistence.register(
                        user,
                        emailVerification);

        /*
         * A transação do adaptador de persistência já terminou neste ponto.
         * O token puro existe somente em memória e nunca entra no banco.
         */
        delivery.ifPresent(target -> verificationNotifier.send(
                target.destinationEmail(),
                rawVerificationToken,
                target.expiresAt()));
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