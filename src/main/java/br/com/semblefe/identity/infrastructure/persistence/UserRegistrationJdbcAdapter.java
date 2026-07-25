package br.com.semblefe.identity.infrastructure.persistence;

import br.com.semblefe.identity.application.model.NewUser;
import br.com.semblefe.identity.application.port.outbound.UserRegistrationPersistence;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public boolean register(NewUser user) {
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
            return false;
        }

        int insertedRoles = jdbcClient.sql("""
                        INSERT INTO usuario_papeis (usuario_id, papel)
                        VALUES (:userId, :role)
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

        if (insertedRoles != 1 || insertedAcceptances != 2) {
            throw new IllegalStateException(
                    "O cadastro não gravou todas as relações obrigatórias.");
        }

        return true;
    }
}
