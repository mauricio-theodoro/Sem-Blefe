package br.com.semblefe.profiles;

import br.com.semblefe.profiles.api.SpecialtyController;
import br.com.semblefe.profiles.application.model.SpecialtySummary;
import br.com.semblefe.profiles.application.port.inbound.ListSpecialtiesUseCase;
import br.com.semblefe.shared.config.SecurityConfiguration;
import br.com.semblefe.shared.web.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpecialtyController.class)
@Import({SecurityConfiguration.class, RequestIdFilter.class})
class SpecialtyControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListSpecialtiesUseCase listSpecialtiesUseCase;

    @Test
    void shouldAllowPublicQueryAndReturnRequestId() throws Exception {
        given(listSpecialtiesUseCase.execute()).willReturn(List.of(
                new SpecialtySummary("DJ", "DJ", "Discotecagem e curadoria musical.")));

        mockMvc.perform(get("/api/v1/public/specialties")
                        .header("X-Request-Id", "teste-semblefe-001"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "teste-semblefe-001"))
                .andExpect(jsonPath("$[0].code").value("DJ"))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void shouldReplaceInvalidRequestId() throws Exception {
        given(listSpecialtiesUseCase.execute()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/public/specialties")
                        .header("X-Request-Id", "valor com espaços e conteúdo não confiável"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(header().string("X-Request-Id", org.hamcrest.Matchers.not("valor com espaços e conteúdo não confiável")));
    }
}
