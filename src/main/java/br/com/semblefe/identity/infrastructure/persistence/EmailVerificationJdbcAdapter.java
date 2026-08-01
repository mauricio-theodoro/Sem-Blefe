package br.com.semblefe.identity.infrastructure.persistence;

import br.com.semblefe.identity.application.port.outbound.EmailVerificationPersistence;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Repository
public class EmailVerificationJdbcAdapter
        implements EmailVerificationPersistence {

    private static final Pattern SHA_256_HEX =
            Pattern.compile("[0-9a-f]{64}");

    private final JdbcClient jdbcClient;

    public EmailVerificationJdbcAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    @Transactional
    public boolean verify(String tokenHash, String requestId) {
        validateHash(tokenHash);

        Optional<AccountState> account =
                findAccountAndLock(tokenHash);

        /*
         * Tokens públicos inexistentes não são gravados na auditoria para
         * impedir amplificação de escrita causada por tráfego anônimo.
         */
        if (account.isEmpty()) {
            return false;
        }

        AccountState accountState = account.get();

        if (!accountState.pendingEmail()
                || accountState.emailVerified()) {
            auditDenied(accountState.id(), requestId);
            return false;
        }

        Optional<UUID> consumedToken = jdbcClient.sql("""
                        UPDATE tokens_verificacao_email
                        SET utilizado_em = statement_timestamp()
                        WHERE token_hash = :tokenHash
                          AND usuario_id = :userId
                          AND utilizado_em IS NULL
                          AND expira_em > statement_timestamp()
                        RETURNING id
                        """)
                .param("tokenHash", tokenHash)
                .param("userId", accountState.id())
                .query(UUID.class)
                .optional();

        if (consumedToken.isEmpty()) {
            auditDenied(accountState.id(), requestId);
            return false;
        }

        Optional<UUID> activatedUser = jdbcClient.sql("""
                        UPDATE usuarios
                        SET situacao = 'ATIVO',
                            email_verificado = TRUE,
                            atualizado_em = statement_timestamp(),
                            versao = versao + 1
                        WHERE id = :userId
                          AND situacao = 'PENDENTE_EMAIL'
                          AND email_verificado = FALSE
                        RETURNING id
                        """)
                .param("userId", accountState.id())
                .query(UUID.class)
                .optional();

        if (activatedUser.isEmpty()) {
            throw new IllegalStateException(
                    "A conta não pôde ser ativada após o consumo do token.");
        }

        /*
         * Ao confirmar um token, qualquer outro token pendente da mesma
         * conta também é invalidado.
         */
        jdbcClient.sql("""
                        UPDATE tokens_verificacao_email
                        SET utilizado_em = statement_timestamp()
                        WHERE usuario_id = :userId
                          AND utilizado_em IS NULL
                        """)
                .param("userId", accountState.id())
                .update();

        auditSuccess(accountState.id(), requestId);
        return true;
    }

    private Optional<AccountState> findAccountAndLock(
            String tokenHash) {

        return jdbcClient.sql("""
                        SELECT
                            u.id,
                            u.situacao,
                            u.email_verificado
                        FROM tokens_verificacao_email t
                        JOIN usuarios u
                          ON u.id = t.usuario_id
                        WHERE t.token_hash = :tokenHash
                        FOR UPDATE OF u
                        """)
                .param("tokenHash", tokenHash)
                .query((resultSet, rowNumber) -> new AccountState(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("situacao"),
                        resultSet.getBoolean("email_verificado")))
                .optional();
    }

    private void auditSuccess(UUID userId, String requestId) {
        int inserted = insertAudit(
                userId,
                "SUCESSO",
                requestId);

        if (inserted != 1) {
            throw new IllegalStateException(
                    "A confirmação não pôde ser registrada na auditoria.");
        }
    }

    private void auditDenied(UUID userId, String requestId) {
        int inserted = insertAudit(
                userId,
                "NEGADO",
                requestId);

        if (inserted != 1) {
            throw new IllegalStateException(
                    "A tentativa negada não pôde ser registrada na auditoria.");
        }
    }

    private int insertAudit(
            UUID userId,
            String result,
            String requestId) {

        return jdbcClient.sql("""
                        INSERT INTO eventos_auditoria (
                            usuario_id,
                            evento,
                            recurso_tipo,
                            recurso_id,
                            resultado,
                            request_id,
                            metadados
                        )
                        VALUES (
                            :userId,
                            'EMAIL_VERIFICATION',
                            'USER_ACCOUNT',
                            :resourceId,
                            :result,
                            :requestId,
                            '{}'::jsonb
                        )
                        """)
                .param("userId", userId)
                .param("resourceId", userId.toString())
                .param("result", result)
                .param("requestId", requestId)
                .update();
    }

    private void validateHash(String tokenHash) {
        if (tokenHash == null
                || !SHA_256_HEX.matcher(tokenHash).matches()) {
            throw new IllegalArgumentException(
                    "O hash do token de confirmação é inválido.");
        }
    }

    private record AccountState(
            UUID id,
            String status,
            boolean emailVerified) {

        boolean pendingEmail() {
            return "PENDENTE_EMAIL".equals(status);
        }
    }
}