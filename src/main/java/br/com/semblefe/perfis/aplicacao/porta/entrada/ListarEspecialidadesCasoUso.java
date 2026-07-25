package br.com.semblefe.perfis.aplicacao.porta.entrada;

import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;

import java.util.List;

/**
 * Porta de entrada do caso de uso exposto para a camada de API.
 */
public interface ListarEspecialidadesCasoUso {

    List<EspecialidadeResumo> executar();
}
