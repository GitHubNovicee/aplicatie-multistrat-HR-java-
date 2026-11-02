package org.hr.application;

import org.hr.domain.relations.CaseType;
import org.hr.domain.relations.EmployeeCase;

/*
 * Business Workflow (RQ-10): cerere/reclamatie -> asignare -> tratare -> rezolvare -> inchidere. UC-25 .. UC-27.
 */
public interface IEmployeeCaseWorkflowService {
    /** UC-25: caz OPEN cu numar unic ER-an-nnnn (CR-04); notifica HR_EMPLOYEE_RELATIONS. */
    Long submitCase(Long employeeId, CaseType type, String subject, String details);

    /** UC-26: OPEN -> ASSIGNED; responsabilul trebuie sa aiba rolul HR_EMPLOYEE_RELATIONS. */
    void assignCase(Long assignerId, Long caseId, Long assigneeId);

    /** UC-26: actiune inregistrata de responsabil; cazul devine IN_PROGRESS. */
    Long registerAction(Long performerId, Long caseId, String description);

    /** UC-27 / VR-30: RESOLVED, doar de responsabilul asignat. */
    void resolveCase(Long resolverId, Long caseId, String resolution);

    /** UC-27: RESOLVED -> CLOSED. */
    void closeCase(Long userId, Long caseId);

    /** Reclamatiile sunt vizibile doar angajatului, responsabilului HR si managerului angajatului. */
    EmployeeCase getCase(Long viewerId, Long caseId);
}
