package org.hr.services;

import org.hr.domain.organization.Employee;
import org.hr.domain.organization.EmployeeProfileView;

import java.time.Period;
import java.util.Collection;
import java.util.List;

/** Domain Computation Service (CR-08) - modulul Organizatie. */
public interface IEmployeeComputingService {
    /** CR-08: vechimea = perioada dintre hireDate si terminationDate (sau data curenta). */
    Period computeSeniority(Employee employee);

    /** CR-08: completeaza campul calculat Employee.seniority. */
    Employee computeSeniorityOf(Employee employee);

    EmployeeProfileView toProfileView(Employee employee);

    List<EmployeeProfileView> toProfileView(Collection<Employee> employees);
}
