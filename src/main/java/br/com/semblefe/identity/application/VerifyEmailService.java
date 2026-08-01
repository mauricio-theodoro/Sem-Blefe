package br.com.semblefe.identity.application;

import br.com.semblefe.identity.application.model.VerifyEmailCommand;
import br.com.semblefe.identity.application.port.inbound.VerifyEmailUseCase;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationPersistence;
import br.com.semblefe.identity.application.port.outbound.EmailVerificationTokenSecurity;
import org.springframework.stereotype.Service;

@Service
public class VerifyEmailService implements VerifyEmailUseCase {

    private final EmailVerificationTokenSecurity tokenSecurity;
    private final EmailVerificationPersistence verificationPersistence;

    public VerifyEmailService(
            EmailVerificationTokenSecurity tokenSecurity,
            EmailVerificationPersistence verificationPersistence) {

        this.tokenSecurity = tokenSecurity;
        this.verificationPersistence = verificationPersistence;
    }

    @Override
    public void execute(VerifyEmailCommand command) {
        String tokenHash = tokenSecurity.hash(command.token());

        boolean verified = verificationPersistence.verify(
                tokenHash,
                command.requestId());

        if (!verified) {
            throw VerifyEmailCommand.invalidToken();
        }
    }
}