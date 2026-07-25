package br.com.semblefe.perfis.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representação exclusiva da persistência.
 *
 * <p>Regras de negócio não devem depender desta classe.</p>
 */
@Entity
@Table(name = "especialidades")
public class EspecialidadeJpaEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 240)
    private String descricao;

    @Column(nullable = false)
    private boolean ativa;

    @Column(name = "ordem_exibicao", nullable = false)
    private short ordemExibicao;

    protected EspecialidadeJpaEntidade() {
    }
}
