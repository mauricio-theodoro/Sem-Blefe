package br.com.semblefe.profiles.api;

import br.com.semblefe.profiles.application.model.SpecialtySummary;

public record SpecialtyResponse(
        String code,
        String name,
        String description) {

    static SpecialtyResponse from(SpecialtySummary summary) {
        return new SpecialtyResponse(
                summary.code(),
                summary.name(),
                summary.description());
    }
}
