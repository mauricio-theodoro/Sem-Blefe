package br.com.semblefe.profiles.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Persistence-only representation of a specialty.
 *
 * <p>Business rules must not depend on this class.</p>
 */
@Entity
@Table(name = "especialidades")
public class SpecialtyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "nome", nullable = false, length = 80)
    private String name;

    @Column(name = "descricao", nullable = false, length = 240)
    private String description;

    @Column(name = "ativa", nullable = false)
    private boolean active;

    @Column(name = "ordem_exibicao", nullable = false)
    private short displayOrder;

    protected SpecialtyJpaEntity() {
    }
}
