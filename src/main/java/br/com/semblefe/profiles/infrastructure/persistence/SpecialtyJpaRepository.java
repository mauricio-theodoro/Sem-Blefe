package br.com.semblefe.profiles.infrastructure.persistence;

import br.com.semblefe.profiles.application.model.SpecialtySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpecialtyJpaRepository extends JpaRepository<SpecialtyJpaEntity, Long> {

    @Query("""
            select new br.com.semblefe.profiles.application.model.SpecialtySummary(
                specialty.code,
                specialty.name,
                specialty.description
            )
            from SpecialtyJpaEntity specialty
            where specialty.active = true
            order by specialty.displayOrder, specialty.name
            """)
    List<SpecialtySummary> findActive();
}
