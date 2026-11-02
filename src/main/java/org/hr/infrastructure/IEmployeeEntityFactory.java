package org.hr.infrastructure;

import org.hr.domain.organization.*;

import java.time.LocalDate;
import java.util.Set;

/** Factory pentru agregatele din modulul Organizatie / Conturi (RQ-01..RQ-03). */
public interface IEmployeeEntityFactory {
    Department buildDepartment(String name, Department parent);

    JobPosition buildJobPosition(String title, String grade);

    /** Angajat nou, cu cod unic generat (EMP-0001...), activ (nearhivat). */
    Employee buildEmployee(String firstName, String lastName, String email, String phone,
                           LocalDate hireDate, Department department, JobPosition jobPosition, Employee manager);

    UserAccount buildUserAccount(Employee employee, String username, String passwordHash, Set<Role> roles);
}
