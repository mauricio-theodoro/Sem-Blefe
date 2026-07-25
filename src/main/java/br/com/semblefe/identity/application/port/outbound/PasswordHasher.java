package br.com.semblefe.identity.application.port.outbound;

public interface PasswordHasher {

    String hash(String password);
}
