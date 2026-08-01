package br.com.semblefe.identity.infrastructure.persistence;

import br.com.semblefe.identity.application.model.EmailVerificationDelivery;
import br.com.semblefe.identity.application.model.NewEmailVerification;
import br.com.semblefe.identity.application.model.NewUser;
import br.com.semblefe.identity.application.port.outbound.UserRegistrationPersistence;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRegistrationJdbcAdapter
        implements UserRegistrationPersistence {

    private final JdbcClient jdbcClient;

    public UserRegistrationJdbcAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    @Transactional
    public Optional<EmailVerificationDelivery> register(
            NewUser user,
            NewEmailVerification emailVerification) {

        if (!user.id().equals(emailVerification.userId())) {
            throw new IllegalArgumentException(
                    "O token de confirmação não pertence ao usuário informado.");
        }

        Optional<UUID> insertedUser = jdbcClient.sql("""
                        INSERT INTO usuarios (
                            id,
                            email,
                            email_normalizado,
                            senha_hash,
                            situacao,
                            email_verificado
                        )
                        VALUES (
                            :id,
                            :email,
                            :normalizedEmail,
                            :passwordHash,
                            :status,
                            :emailVerified
                        )
                        ON CONFLICT (email_normalizado) DO NOTHING
                        RETURNING id
                        """)
                .param("id", user.id())
                .param("email", user.email().value())
                .param("normalizedEmail", user.email().normalized())
                .param("passwordHash", user.passwordHash())
                .param("status", user.initialStatus())
                .param("emailVerified", user.emailVerified())
                .query(UUID.class)
                .optional();

        if (insertedUser.isEmpty()) {
            return Optional.empty();
        }

        int insertedRoles = jdbcClient.sql("""
                        INSERT INTO usuario_papeis (
                            usuario_id,
                            papel
                        )
                        VALUES (
                            :userId,
                            :role
                        )
                        """)
                .param("userId", user.id())
                .param("role", user.initialRole())
                .update();

        int insertedAcceptances = jdbcClient.sql("""
                        INSERT INTO aceites_legais (
                            id,
                            usuario_id,
                            documento,
                            versao_documento,
                            origem
                        )
                        VALUES
                            (
                                :termsId,
                                :userId,
                                'TERMOS_USO',
                                :termsVersion,
                                :source
                            ),
                            (
                                :privacyId,
                                :userId,
                                'POLITICA_PRIVACIDADE',
                                :privacyVersion,
                                :source
                            )
                        """)
                .param("termsId", UUID.randomUUID())
                .param("privacyId", UUID.randomUUID())
                .param("userId", user.id())
                .param("termsVersion", user.termsOfUseVersion())
                .param("privacyVersion", user.privacyPolicyVersion())
                .param("source", user.legalAcceptanceSource())
                .update();

        Instant expiresAt = jdbcClient.sql("""
                        INSERT INTO tokens_verificacao_email (
                            id,
                            usuario_id,
                            token_hash,
                            criado_em,
                            expira_em
                        )
                        VALUES (
                            :id,
                            :userId,
                            :tokenHash,
                            statement_timestamp(),
                            statement_timestamp()
                                + make_interval(secs => :validitySeconds)
                        )
                        RETURNING expira_em
                        """)
                .param("id", emailVerification.id())
                .param("userId", emailVerification.userId())
                .param("tokenHash", emailVerification.tokenHash())
                .param(
                        "validitySeconds",
                        seconds(emailVerification.tokenValidity()))
                .query((resultSet, rowNumber) ->
                        resultSet.getTimestamp("expira_em").toInstant())
                .single();

        if (insertedRoles != 1 || insertedAcceptances != 2) {
            throw new IllegalStateException(
                    "O cadastro não gravou todas as relações obrigatórias.");
        }

        return Optional.of(new EmailVerificationDelivery(
                user.email().value(),
                expiresAt));
    }

    private double seconds(Duration duration) {
        return duration.getSeconds()
                + duration.getNano() / 1_000_000_000.0d;
    }
}