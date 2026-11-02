package org.hr.infrastructure;

import org.hr.domain.common.CaseNumber;
import org.hr.domain.organization.Employee;
import org.hr.domain.relations.CaseAction;
import org.hr.domain.relations.CaseType;
import org.hr.domain.relations.EmployeeCase;

/** Factory pentru agregatul EmployeeCase (RQ-10). */
public interface IEmployeeCaseEntityFactory {
    /** CR-04: urmatorul numar de caz pentru anul dat (ER-&lt;an&gt;-0001, 0002...). */
    CaseNumber nextCaseNumber(int year);

    /** Caz OPEN cu numar unic generat (CR-04). */
    EmployeeCase buildEmployeeCase(CaseType type, Employee submittedBy, String subject, String details);

    /** Componenta (id local atribuit de EmployeeCase.addAction). */
    CaseAction buildCaseAction(Employee performedBy, String description);
}
