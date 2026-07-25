package br.com.semblefe.identity.api;

import br.com.semblefe.identity.application.model.RegisterUserCommand;
import br.com.semblefe.identity.application.port.inbound.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/registrations")
@Tag(name = "Registration", description = "Criação pública de contas da comunidade")
public class UserRegistrationController {

    private final RegisterUserUseCase registerUserUseCase;

    public UserRegistrationController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Recebe o cadastro inicial de uma conta")
    public UserRegistrationResponse register(
            @Valid @RequestBody UserRegistrationRequest request) {

        registerUserUseCase.execute(new RegisterUserCommand(
                request.email(),
                request.password(),
                request.termsOfUseVersion(),
                request.privacyPolicyVersion(),
                request.acceptedTermsOfUse(),
                request.acceptedPrivacyPolicy()));

        return UserRegistrationResponse.registrationReceived();
    }
}
