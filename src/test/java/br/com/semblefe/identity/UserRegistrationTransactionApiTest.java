package br.com.semblefe.identity;

import br.com.semblefe.IntegrationTestBase;
import br.com.semblefe.identity.application.model.RegisterUserCommand;
import br.com.semblefe.identity.application.port.inbound.RegisterUserUseCase;
import br.com.semblefe.identity.application.port.outbound.CurrentLegalDocuments;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(UserRegistrationTransactionApiTest.AcceptanceFailureConfiguration.class)
@TestPropertySource(properties = {
        "semblefe.legal-documents.terms-of-use-version=1.0",
        "semblefe.legal-documents.privacy-policy-version=1.0"
})
class UserRegistrationTransactionApiTest extends IntegrationTestBase {

    private static final String EMAIL = "rollback.cadastro@semblefe.com";

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRollbackUserAndRoleWhenLegalAcceptancePersistenceFails() {
        assertThatThrownBy(() -> registerUserUseCase.execute(
                new RegisterUserCommand(
                        EMAIL,
                        "Minha frase musical para rollback!",
                        "v".repeat(31),
                        "1.0",
                        true,
                        true)))
                .isInstanceOf(DataAccessException.class);

        Integer users = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM usuarios
                        WHERE email_normalizado = ?
                        """,
                Integer.class,
                EMAIL);

        Integer roles = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM usuario_papeis up
                        JOIN usuarios u ON u.id = up.usuario_id
                        WHERE u.email_normalizado = ?
                        """,
                Integer.class,
                EMAIL);

        Integer acceptances = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM aceites_legais al
                        JOIN usuarios u ON u.id = al.usuario_id
                        WHERE u.email_normalizado = ?
                        """,
                Integer.class,
                EMAIL);

        assertThat(users).isZero();
        assertThat(roles).isZero();
        assertThat(acceptances).isZero();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class AcceptanceFailureConfiguration {

        @Bean
        @Primary
        CurrentLegalDocuments invalidVersionLegalDocuments() {
            return new CurrentLegalDocuments() {
                @Override
                public String termsOfUseVersion() {
                    return "v".repeat(31);
                }

                @Override
                public String privacyPolicyVersion() {
                    return "1.0";
                }
            };
        }
    }
}
