package org.hr.infrastructure;

import org.hr.domain.relations.EmployeeCase;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class EmployeeCaseEntityRepositoryBase extends InMemoryEntityRepositoryBase<EmployeeCase> implements IEmployeeCaseEntityRepository {
    public EmployeeCaseEntityRepositoryBase() {
        super("EmployeeCase");
    }
}
