package br.com.semblefe.identity.application.port.inbound;

import br.com.semblefe.identity.application.model.RegisterUserCommand;

public interface RegisterUserUseCase {

    void execute(RegisterUserCommand command);
}
