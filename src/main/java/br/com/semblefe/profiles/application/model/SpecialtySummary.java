package br.com.semblefe.profiles.application.model;

/**
 * Immutable projection used by the specialty query use case.
 *
 * <p>It is independent of HTTP, JPA, and database implementation details.</p>
 */
public record SpecialtySummary(
        String code,
        String name,
        String description) {
}
