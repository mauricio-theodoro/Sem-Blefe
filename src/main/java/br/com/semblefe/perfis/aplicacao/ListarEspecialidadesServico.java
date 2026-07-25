package br.com.semblefe.perfis.aplicacao;

import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;
import br.com.semblefe.perfis.aplicacao.porta.entrada.ListarEspecialidadesCasoUso;
import br.com.semblefe.perfis.aplicacao.porta.saida.EspecialidadeConsulta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarEspecialidadesServico implements ListarEspecialidadesCasoUso {

    private final EspecialidadeConsulta consulta;

    public ListarEspecialidadesServico(EspecialidadeConsulta consulta) {
        this.consulta = consulta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadeResumo> executar() {
        return consulta.listarAtivas();
    }
}
