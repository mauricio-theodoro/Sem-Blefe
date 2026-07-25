package br.com.semblefe.profiles;

import br.com.semblefe.profiles.application.ListSpecialtiesService;
import br.com.semblefe.profiles.application.model.SpecialtySummary;
import br.com.semblefe.profiles.application.port.outbound.SpecialtyQuery;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListSpecialtiesServiceTest {

    @Test
    void shouldDelegateQueryThroughOutboundPort() {
        SpecialtyQuery specialtyQuery = () -> List.of(
                new SpecialtySummary(
                        "BEATMAKER",
                        "Beatmaker",
                        "Criador e produtor de beats e instrumentais."));

        var service = new ListSpecialtiesService(specialtyQuery);

        var result = service.execute();

        assertThat(result).containsExactly(
                new SpecialtySummary(
                        "BEATMAKER",
                        "Beatmaker",
                        "Criador e produtor de beats e instrumentais."));
    }
}
