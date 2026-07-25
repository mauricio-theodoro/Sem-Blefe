package br.com.semblefe.perfis;

import br.com.semblefe.perfis.aplicacao.ListarEspecialidadesServico;
import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;
import br.com.semblefe.perfis.aplicacao.porta.saida.EspecialidadeConsulta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListarEspecialidadesServicoTest {

    @Test
    void deveRepassarAConsultaPelaPortaDeSaida() {
        EspecialidadeConsulta consulta = () -> List.of(
                new EspecialidadeResumo(
                        "BEATMAKER",
                        "Beatmaker",
                        "Criador e produtor de beats e instrumentais."));

        var servico = new ListarEspecialidadesServico(consulta);

        var resultado = servico.executar();

        assertThat(resultado).containsExactly(
                new EspecialidadeResumo(
                        "BEATMAKER",
                        "Beatmaker",
                        "Criador e produtor de beats e instrumentais."));
    }
}
