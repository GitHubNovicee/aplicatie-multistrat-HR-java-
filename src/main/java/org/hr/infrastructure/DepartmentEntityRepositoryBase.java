package org.hr.infrastructure;

import org.hr.domain.organization.Department;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class DepartmentEntityRepositoryBase extends InMemoryEntityRepositoryBase<Department> implements IDepartmentEntityRepository {
    public DepartmentEntityRepositoryBase() {
        super("Department");
    }
}
