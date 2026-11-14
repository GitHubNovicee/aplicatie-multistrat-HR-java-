package org.hr.infrastructure;

import org.hr.domain.organization.Employee;
import org.hr.domain.training.Competency;
import org.hr.domain.training.EmployeeCompetency;

import java.util.List;
import java.util.Optional;

public interface IEmployeeCompetencyEntityRepository extends IEntityRepository<EmployeeCompetency> {
    List<EmployeeCompetency> findByEmployee(Employee employee);

    Optional<EmployeeCompetency> findByEmployeeAndCompetency(Employee employee, Competency competency);
}
