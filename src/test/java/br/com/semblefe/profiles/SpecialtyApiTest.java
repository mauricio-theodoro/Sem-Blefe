package br.com.semblefe.profiles;

import br.com.semblefe.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SpecialtyApiTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListActiveSpecialtiesWithoutExposingEntities() throws Exception {
        mockMvc.perform(get("/api/v1/public/specialties"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(17)))
                .andExpect(jsonPath("$[0].code").value("FAN"))
                .andExpect(jsonPath("$[0].name").value("Fã"))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void shouldRejectNonPublicRouteByDefault() throws Exception {
        mockMvc.perform(get("/api/v1/private/test"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldExposeAvailabilityProbesWithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }
}
