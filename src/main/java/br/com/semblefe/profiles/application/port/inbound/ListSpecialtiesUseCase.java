package br.com.semblefe.profiles.application.port.inbound;

import br.com.semblefe.profiles.application.model.SpecialtySummary;

import java.util.List;

/**
 * Inbound port that exposes specialty listing to delivery adapters.
 */
public interface ListSpecialtiesUseCase {

    List<SpecialtySummary> execute();
}
