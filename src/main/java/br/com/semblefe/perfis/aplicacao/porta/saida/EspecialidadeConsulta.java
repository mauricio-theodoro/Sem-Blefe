package br.com.semblefe.perfis.aplicacao.porta.saida;

import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;

import java.util.List;

/**
 * Porta de saída para consultar especialidades sem acoplar a aplicação ao JPA.
 */
public interface EspecialidadeConsulta {

    List<EspecialidadeResumo> listarAtivas();
}
