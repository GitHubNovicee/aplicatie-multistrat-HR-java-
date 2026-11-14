package org.hr.infrastructure;

import org.hr.domain.training.Competency;

import java.util.List;
import java.util.Optional;

public interface ICompetencyEntityRepository extends IEntityRepository<Competency> {
    Optional<Competency> findByName(String name);

    List<Competency> findByCategory(String category);
}
