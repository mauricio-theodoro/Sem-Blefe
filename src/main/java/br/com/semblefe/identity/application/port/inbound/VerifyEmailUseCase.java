package br.com.semblefe.identity.application.port.inbound;

import br.com.semblefe.identity.application.model.VerifyEmailCommand;

public interface VerifyEmailUseCase {

    void execute(VerifyEmailCommand command);
}