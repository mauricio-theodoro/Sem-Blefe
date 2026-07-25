package br.com.semblefe.perfis.infraestrutura.persistencia;

import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;
import br.com.semblefe.perfis.aplicacao.porta.saida.EspecialidadeConsulta;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Adaptador que conecta a porta da aplicação ao Spring Data JPA.
 */
@Repository
public class EspecialidadeConsultaJpaAdaptador implements EspecialidadeConsulta {

    private final EspecialidadeJpaRepositorio repositorio;

    public EspecialidadeConsultaJpaAdaptador(EspecialidadeJpaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<EspecialidadeResumo> listarAtivas() {
        return repositorio.listarAtivas();
    }
}
