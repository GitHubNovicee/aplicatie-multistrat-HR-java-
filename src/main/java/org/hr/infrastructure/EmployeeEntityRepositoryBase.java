package org.hr.infrastructure;

import org.hr.domain.organization.Employee;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class EmployeeEntityRepositoryBase extends InMemoryEntityRepositoryBase<Employee> implements IEmployeeEntityRepository {
    public EmployeeEntityRepositoryBase() {
        super("Employee");
    }

    /** VR-28 / RQ-01: angajatii nu se sterg fizic; se arhiveaza logic (Employee.archived). */
    @Override
    public boolean remove(Employee entity) {
        throw new UnsupportedOperationException("Employees are never physically deleted; archive them instead!");
    }
}
