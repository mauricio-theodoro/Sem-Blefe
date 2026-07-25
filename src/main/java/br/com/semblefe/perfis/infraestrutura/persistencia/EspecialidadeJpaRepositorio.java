package br.com.semblefe.perfis.infraestrutura.persistencia;

import br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EspecialidadeJpaRepositorio extends JpaRepository<EspecialidadeJpaEntidade, Long> {

    @Query("""
            select new br.com.semblefe.perfis.aplicacao.modelo.EspecialidadeResumo(
                e.codigo,
                e.nome,
                e.descricao
            )
            from EspecialidadeJpaEntidade e
            where e.ativa = true
            order by e.ordemExibicao, e.nome
            """)
    List<EspecialidadeResumo> listarAtivas();
}
