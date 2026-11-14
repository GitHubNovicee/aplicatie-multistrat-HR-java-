package org.hr.infrastructure;

import org.hr.domain.common.CaseNumber;
import org.hr.domain.organization.Employee;
import org.hr.domain.relations.CaseStatus;
import org.hr.domain.relations.EmployeeCase;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IEmployeeCaseEntityRepository extends IEntityRepository<EmployeeCase> {
    Optional<EmployeeCase> findByCaseNumber(CaseNumber caseNumber);

    List<EmployeeCase> findBySubmittedBy(Employee submittedBy);

    List<EmployeeCase> findByAssignedToAndStatusIn(Employee assignedTo, Collection<CaseStatus> statuses);
}
