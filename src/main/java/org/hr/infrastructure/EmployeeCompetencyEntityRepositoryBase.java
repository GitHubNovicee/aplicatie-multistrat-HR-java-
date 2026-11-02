package org.hr.infrastructure;

import org.hr.domain.training.EmployeeCompetency;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class EmployeeCompetencyEntityRepositoryBase extends InMemoryEntityRepositoryBase<EmployeeCompetency> implements IEmployeeCompetencyEntityRepository {
    public EmployeeCompetencyEntityRepositoryBase() {
        super("EmployeeCompetency");
    }
}
