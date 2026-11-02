package org.hr.services;

import org.hr.domain.organization.Employee;
import org.hr.domain.relations.EmployeeCase;

/** Domain Validation Service - modulul Relatii angajati (RQ-10, VR-28, VR-29, VR-30). */
public interface IEmployeeCaseValidatingService {
    /** Bean validation (inclusiv cascada pe actiuni - VR-29) + VR-28 (angajat activ) + numar de caz unic. */
    void validateCaseSubmission(EmployeeCase employeeCase);

    /** Bean validation pe cazul si actiunile sale. */
    void validateCase(EmployeeCase employeeCase);

    /**
     * VR-30: cazul poate fi rezolvat doar de responsabilul asignat; resolution obligatorie;
     * cazul trebuie sa fie IN_PROGRESS.
     */
    void validateResolution(EmployeeCase employeeCase, Employee resolver, String resolution);
}
