package br.com.semblefe.perfis;

import br.com.semblefe.IntegracaoBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EspecialidadeApiTest extends IntegracaoBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveListarEspecialidadesAtivasSemExporEntidades() throws Exception {
        mockMvc.perform(get("/api/v1/publico/especialidades"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(17)))
                .andExpect(jsonPath("$[0].codigo").value("FAN"))
                .andExpect(jsonPath("$[0].nome").value("Fã"))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void deveRecusarRotaNaoLiberadaPorPadrao() throws Exception {
        mockMvc.perform(get("/api/v1/privado/teste"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deveExporSondasDeDisponibilidadeSemDetalhesInternos() throws Exception {
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
