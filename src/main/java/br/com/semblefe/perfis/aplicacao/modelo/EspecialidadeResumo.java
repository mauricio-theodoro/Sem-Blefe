package br.com.semblefe.perfis.aplicacao.modelo;

/**
 * Projeção imutável usada pelo caso de uso de consulta.
 *
 * <p>Ela não conhece HTTP, JPA ou detalhes do banco.</p>
 */
public record EspecialidadeResumo(
        String codigo,
        String nome,
        String descricao) {
}
