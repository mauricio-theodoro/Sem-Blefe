package br.com.semblefe.identity.api;

import br.com.semblefe.identity.application.model.VerifyEmailCommand;
import br.com.semblefe.identity.application.port.inbound.VerifyEmailUseCase;
import br.com.semblefe.shared.web.RequestIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/email-verifications")
@Tag(
        name = "Email verification",
        description = "Confirmação do endereço de e-mail")
public class EmailVerificationController {

    private final VerifyEmailUseCase verifyEmailUseCase;

    public EmailVerificationController(
            VerifyEmailUseCase verifyEmailUseCase) {

        this.verifyEmailUseCase = verifyEmailUseCase;
    }

    @PostMapping
    @Operation(summary = "Confirma o e-mail e ativa uma conta pendente")
    public EmailVerificationResponse verify(
            @Valid @RequestBody EmailVerificationRequest request,
            @RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE)
            String requestId) {

        verifyEmailUseCase.execute(new VerifyEmailCommand(
                request.token(),
                requestId));

        return EmailVerificationResponse.emailVerified();
    }
}