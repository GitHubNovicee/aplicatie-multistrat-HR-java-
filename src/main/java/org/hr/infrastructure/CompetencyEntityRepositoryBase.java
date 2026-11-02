package org.hr.infrastructure;

import org.hr.domain.training.Competency;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class CompetencyEntityRepositoryBase extends InMemoryEntityRepositoryBase<Competency> implements ICompetencyEntityRepository {
    public CompetencyEntityRepositoryBase() {
        super("Competency");
    }
}
