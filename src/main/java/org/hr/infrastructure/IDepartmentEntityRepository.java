package org.hr.infrastructure;

import org.hr.domain.organization.Department;

import java.util.List;
import java.util.Optional;

public interface IDepartmentEntityRepository extends IEntityRepository<Department> {
    Optional<Department> findByName(String name);

    List<Department> findByParent(Department parent);
}
