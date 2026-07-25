package br.com.semblefe.profiles.application.port.outbound;

import br.com.semblefe.profiles.application.model.SpecialtySummary;

import java.util.List;

/**
 * Outbound port for querying specialties without coupling the application to JPA.
 */
public interface SpecialtyQuery {

    List<SpecialtySummary> findActive();
}
