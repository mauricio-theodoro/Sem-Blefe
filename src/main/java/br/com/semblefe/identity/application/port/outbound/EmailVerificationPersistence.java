package br.com.semblefe.identity.application.port.outbound;

public interface EmailVerificationPersistence {

    /**
     * Consumes a pending token, activates its account and writes the audit
     * event in one transaction.
     *
     * @return true only when this attempt activated the account.
     */
    boolean verify(String tokenHash, String requestId);
}