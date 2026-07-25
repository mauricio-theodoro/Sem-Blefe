package br.com.semblefe.identity;

import br.com.semblefe.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "semblefe.legal-documents.terms-of-use-version=1.0",
        "semblefe.legal-documents.privacy-policy-version=1.0"
})
class UserRegistrationApiTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterPendingAccountWithRoleAndLegalAcceptances()
            throws Exception {
        String normalizedEmail = "cadastro.integracao@semblefe.com";
        String password = "Minha frase musical segura 2026!";

        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request(
                                " Cadastro.Integracao@SemBlefe.COM ",
                                password)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value("REGISTRATION_RECEIVED"))
                .andExpect(jsonPath("$.id").doesNotExist());

        UserData user = jdbcTemplate.queryForObject("""
                        SELECT
                            email,
                            email_normalizado,
                            senha_hash,
                            situacao,
                            email_verificado
                        FROM usuarios
                        WHERE email_normalizado = ?
                        """,
                (resultSet, rowNumber) -> new UserData(
                        resultSet.getString("email"),
                        resultSet.getString("email_normalizado"),
                        resultSet.getString("senha_hash"),
                        resultSet.getString("situacao"),
                        resultSet.getBoolean("email_verificado")),
                normalizedEmail);

        assertThat(user).isNotNull();
        assertThat(user.email()).isEqualTo(
                "Cadastro.Integracao@SemBlefe.COM");
        assertThat(user.normalizedEmail()).isEqualTo(normalizedEmail);
        assertThat(user.passwordHash()).startsWith("{argon2}$argon2id$");
        assertThat(user.passwordHash()).doesNotContain(password);
        assertThat(passwordEncoder.matches(password, user.passwordHash()))
                .isTrue();
        assertThat(user.status()).isEqualTo("PENDENTE_EMAIL");
        assertThat(user.emailVerified()).isFalse();

        List<String> roles = jdbcTemplate.queryForList("""
                        SELECT up.papel
                        FROM usuario_papeis up
                        JOIN usuarios u ON u.id = up.usuario_id
                        WHERE u.email_normalizado = ?
                        """,
                String.class,
                normalizedEmail);

        assertThat(roles).containsExactly("USUARIO");

        List<String> acceptances = jdbcTemplate.queryForList("""
                        SELECT
                            al.documento || ':' ||
                            al.versao_documento || ':' ||
                            al.origem
                        FROM aceites_legais al
                        JOIN usuarios u ON u.id = al.usuario_id
                        WHERE u.email_normalizado = ?
                        """,
                String.class,
                normalizedEmail);

        assertThat(acceptances).containsExactlyInAnyOrder(
                "TERMOS_USO:1.0:PWA",
                "POLITICA_PRIVACIDADE:1.0:PWA");
    }

    @Test
    void shouldReturnSameResponseAndKeepAccountWhenEmailAlreadyExists()
            throws Exception {
        String normalizedEmail = "duplicado@semblefe.com";
        String initialPassword = "Primeira frase musical segura!";

        String firstResponse = mockMvc.perform(
                        post("/api/v1/public/registrations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(
                                        normalizedEmail,
                                        initialPassword)))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String initialHash = jdbcTemplate.queryForObject("""
                        SELECT senha_hash
                        FROM usuarios
                        WHERE email_normalizado = ?
                        """,
                String.class,
                normalizedEmail);

        String secondResponse = mockMvc.perform(
                        post("/api/v1/public/registrations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request(
                                        "  DUPLICADO@SEMBLEFE.COM ",
                                        "Segunda frase que não substituirá!")))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer users = countUsers(normalizedEmail);
        Integer roles = countRoles(normalizedEmail);
        Integer acceptances = countAcceptances(normalizedEmail);

        String resultingHash = jdbcTemplate.queryForObject("""
                        SELECT senha_hash
                        FROM usuarios
                        WHERE email_normalizado = ?
                        """,
                String.class,
                normalizedEmail);

        assertThat(secondResponse).isEqualTo(firstResponse);
        assertThat(users).isEqualTo(1);
        assertThat(roles).isEqualTo(1);
        assertThat(acceptances).isEqualTo(2);
        assertThat(resultingHash).isEqualTo(initialHash);
    }

    @Test
    void shouldNotPersistInvalidRequest() throws Exception {
        String normalizedEmail = "invalido@semblefe.com";

        mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalido@semblefe.com",
                                  "password": "curta",
                                  "termsOfUseVersion": "1.0",
                                  "privacyPolicyVersion": "1.0",
                                  "acceptedTermsOfUse": true,
                                  "acceptedPrivacyPolicy": true
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(countUsers(normalizedEmail)).isZero();
    }

    @Test
    void shouldCreateOnlyOneAccountForConcurrentRegistrations()
            throws Exception {
        String normalizedEmail = "concorrente@semblefe.com";
        CountDownLatch requestsReady = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> first = executor.submit(
                    () -> executeConcurrentRegistration(
                            normalizedEmail,
                            "Primeira frase concorrente segura!",
                            requestsReady,
                            start));
            Future<Integer> second = executor.submit(
                    () -> executeConcurrentRegistration(
                            " CONCORRENTE@SEMBLEFE.COM ",
                            "Segunda frase concorrente segura!",
                            requestsReady,
                            start));

            assertThat(requestsReady.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            assertThat(first.get(30, TimeUnit.SECONDS)).isEqualTo(202);
            assertThat(second.get(30, TimeUnit.SECONDS)).isEqualTo(202);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }

        assertThat(countUsers(normalizedEmail)).isEqualTo(1);
        assertThat(countRoles(normalizedEmail)).isEqualTo(1);
        assertThat(countAcceptances(normalizedEmail)).isEqualTo(2);
    }

    private int executeConcurrentRegistration(
            String email,
            String password,
            CountDownLatch requestsReady,
            CountDownLatch start) throws Exception {
        requestsReady.countDown();
        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "As requisições não foram liberadas.");
        }

        return mockMvc.perform(post("/api/v1/public/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request(email, password)))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    private Integer countUsers(String normalizedEmail) {
        return jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM usuarios
                        WHERE email_normalizado = ?
                        """,
                Integer.class,
                normalizedEmail);
    }

    private Integer countRoles(String normalizedEmail) {
        return jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM usuario_papeis up
                        JOIN usuarios u ON u.id = up.usuario_id
                        WHERE u.email_normalizado = ?
                        """,
                Integer.class,
                normalizedEmail);
    }

    private Integer countAcceptances(String normalizedEmail) {
        return jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM aceites_legais al
                        JOIN usuarios u ON u.id = al.usuario_id
                        WHERE u.email_normalizado = ?
                        """,
                Integer.class,
                normalizedEmail);
    }

    private String request(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s",
                  "termsOfUseVersion": "1.0",
                  "privacyPolicyVersion": "1.0",
                  "acceptedTermsOfUse": true,
                  "acceptedPrivacyPolicy": true
                }
                """.formatted(email, password);
    }

    private record UserData(
            String email,
            String normalizedEmail,
            String passwordHash,
            String status,
            boolean emailVerified) {
    }
}
