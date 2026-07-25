package br.com.semblefe.profiles.application;

import br.com.semblefe.profiles.application.model.SpecialtySummary;
import br.com.semblefe.profiles.application.port.inbound.ListSpecialtiesUseCase;
import br.com.semblefe.profiles.application.port.outbound.SpecialtyQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSpecialtiesService implements ListSpecialtiesUseCase {

    private final SpecialtyQuery specialtyQuery;

    public ListSpecialtiesService(SpecialtyQuery specialtyQuery) {
        this.specialtyQuery = specialtyQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtySummary> execute() {
        return specialtyQuery.findActive();
    }
}
