package br.com.semblefe.profiles.infrastructure.persistence;

import br.com.semblefe.profiles.application.model.SpecialtySummary;
import br.com.semblefe.profiles.application.port.outbound.SpecialtyQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Adapter that connects the application query port to Spring Data JPA.
 */
@Repository
public class SpecialtyQueryJpaAdapter implements SpecialtyQuery {

    private final SpecialtyJpaRepository specialtyRepository;

    public SpecialtyQueryJpaAdapter(SpecialtyJpaRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    public List<SpecialtySummary> findActive() {
        return specialtyRepository.findActive();
    }
}
