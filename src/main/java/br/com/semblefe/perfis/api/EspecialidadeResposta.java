package br.com.semblefe.perfis.api;

import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;

public record EspecialidadeResposta(
        String codigo,
        String nome,
        String descricao) {

    static EspecialidadeResposta de(EspecialidadeResumo resumo) {
        return new EspecialidadeResposta(
                resumo.codigo(),
                resumo.nome(),
                resumo.descricao());
    }
}
