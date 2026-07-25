package br.com.semblefe.perfis;

import br.com.semblefe.compartilhado.configuracao.SegurancaConfiguracao;
import br.com.semblefe.compartilhado.web.IdentificadorRequisicaoFiltro;
import br.com.semblefe.perfis.api.EspecialidadeController;
import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;
import br.com.semblefe.perfis.aplicacao.porta.entrada.ListarEspecialidadesCasoUso;
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

@WebMvcTest(EspecialidadeController.class)
@Import({SegurancaConfiguracao.class, IdentificadorRequisicaoFiltro.class})
class EspecialidadeControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListarEspecialidadesCasoUso casoUso;

    @Test
    void devePermitirConsultaPublicaERetornarRequestId() throws Exception {
        given(casoUso.executar()).willReturn(List.of(
                new EspecialidadeResumo("DJ", "DJ", "Discotecagem e curadoria musical.")));

        mockMvc.perform(get("/api/v1/publico/especialidades")
                        .header("X-Request-Id", "teste-semblefe-001"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "teste-semblefe-001"))
                .andExpect(jsonPath("$[0].codigo").value("DJ"))
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    @Test
    void deveSubstituirRequestIdInvalido() throws Exception {
        given(casoUso.executar()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/publico/especialidades")
                        .header("X-Request-Id", "valor com espaços e conteúdo não confiável"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(header().string("X-Request-Id", org.hamcrest.Matchers.not("valor com espaços e conteúdo não confiável")));
    }
}
