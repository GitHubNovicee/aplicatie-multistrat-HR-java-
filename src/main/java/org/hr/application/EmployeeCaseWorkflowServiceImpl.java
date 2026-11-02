package org.hr.application;

import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.common.HrAuditView.AuditOperation;
import org.hr.domain.notification.NotificationType;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.relations.*;
import org.hr.infrastructure.IEmployeeCaseEntityFactory;
import org.hr.infrastructure.IEmployeeCaseEntityRepository;
import org.hr.infrastructure.IEmployeeEntityRepository;
import org.hr.services.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class EmployeeCaseWorkflowServiceImpl implements IEmployeeCaseWorkflowService {
    private final IEmployeeEntityRepository employeeRepository;
    private final IEmployeeCaseEntityRepository caseRepository;
    private final IEmployeeCaseEntityFactory factory;
    private final IEmployeeCaseValidatingService validator;
    private final IAccessControlService accessControl;
    private final INotificationService notificationService;
    private final IHrAuditingService auditingService;
    private final Clock clock;

    public EmployeeCaseWorkflowServiceImpl(IEmployeeEntityRepository employeeRepository,
                                           IEmployeeCaseEntityRepository caseRepository,
                                           IEmployeeCaseEntityFactory factory,
                                           IEmployeeCaseValidatingService validator,
                                           IAccessControlService accessControl,
                                           INotificationService notificationService,
                                           IHrAuditingService auditingService, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.caseRepository = caseRepository;
        this.factory = factory;
        this.validator = validator;
        this.accessControl = accessControl;
        this.notificationService = notificationService;
        this.auditingService = auditingService;
        this.clock = clock;
    }

    @Override
    public synchronized Long submitCase(Long employeeId, CaseType type, String subject, String details) {
        EmployeeCase employeeCase = factory.buildEmployeeCase(type, employeeRepository.requireById(employeeId), subject, details);
        validator.validateCaseSubmission(employeeCase);                   // VR-28
        caseRepository.add(employeeCase);
        notificationService.notifyRole(Role.HR_EMPLOYEE_RELATIONS, NotificationType.GENERAL,
                "New " + type + " " + employeeCase.getCaseNumber(), "EMPLOYEE_CASE", employeeCase.getId());
        auditingService.audit("EmployeeCase", employeeCase.getId(), AuditOperation.CREATED, employeeCase.getCaseNumber().toString());
        return employeeCase.getId();
    }

    @Override
    public void assignCase(Long assignerId, Long caseId, Long assigneeId) {
        accessControl.requireAnyRole(assignerId, Role.HR_EMPLOYEE_RELATIONS, Role.HR_ADMIN);
        accessControl.requireAnyRole(assigneeId, Role.HR_EMPLOYEE_RELATIONS);
        EmployeeCase employeeCase = caseRepository.requireById(caseId);
        if (employeeCase.getStatus() != CaseStatus.OPEN && employeeCase.getStatus() != CaseStatus.ASSIGNED)
            throw new BusinessValidationException("Only OPEN/ASSIGNED cases can be (re)assigned!");
        Employee assignee = employeeRepository.requireById(assigneeId);
        employeeCase.setAssignedTo(assignee);
        employeeCase.setStatus(CaseStatus.ASSIGNED);
        caseRepository.add(employeeCase);
        notificationService.notifyEmployee(assignee, NotificationType.GENERAL,
                "Case " + employeeCase.getCaseNumber() + " was assigned to you", "EMPLOYEE_CASE", caseId);
        auditingService.audit("EmployeeCase", caseId, AuditOperation.ASSIGNED, assignee.getEmployeeCode());
    }

    @Override
    public Long registerAction(Long performerId, Long caseId, String description) {
        EmployeeCase employeeCase = caseRepository.requireById(caseId);
        if (employeeCase.getAssignedTo() == null || !employeeCase.getAssignedTo().getId().equals(performerId))
            throw new AccessDeniedBusinessException("Only the assigned HR responsible can register actions!");
        if (employeeCase.getStatus() != CaseStatus.ASSIGNED && employeeCase.getStatus() != CaseStatus.IN_PROGRESS)
            throw new BusinessValidationException("Actions can be registered only for ASSIGNED/IN_PROGRESS cases!");
        CaseAction action = employeeCase.addAction(factory.buildCaseAction(employeeCase.getAssignedTo(), description));
        employeeCase.setStatus(CaseStatus.IN_PROGRESS);
        validator.validateCase(employeeCase);
        caseRepository.add(employeeCase);
        auditingService.audit("EmployeeCase", caseId, AuditOperation.UPDATED, "action " + action.getId());
        return action.getId();
    }

    @Override
    public void resolveCase(Long resolverId, Long caseId, String resolution) {
        EmployeeCase employeeCase = caseRepository.requireById(caseId);
        validator.validateResolution(employeeCase, employeeRepository.requireById(resolverId), resolution);   // VR-30
        employeeCase.setResolution(resolution);
        employeeCase.setResolvedAt(Instant.now(clock));
        employeeCase.setStatus(CaseStatus.RESOLVED);
        validator.validateCase(employeeCase);
        caseRepository.add(employeeCase);
        notificationService.notifyEmployee(employeeCase.getSubmittedBy(), NotificationType.GENERAL,
                "Case " + employeeCase.getCaseNumber() + " was resolved", "EMPLOYEE_CASE", caseId);
        auditingService.audit("EmployeeCase", caseId, AuditOperation.RESOLVED, "by " + resolverId);
    }

    @Override
    public void closeCase(Long userId, Long caseId) {
        EmployeeCase employeeCase = caseRepository.requireById(caseId);
        boolean assignee = employeeCase.getAssignedTo() != null && employeeCase.getAssignedTo().getId().equals(userId);
        if (!assignee)
            accessControl.requireAnyRole(userId, Role.HR_ADMIN);
        if (employeeCase.getStatus() != CaseStatus.RESOLVED)
            throw new BusinessValidationException("Only RESOLVED cases can be closed!");
        employeeCase.setStatus(CaseStatus.CLOSED);
        caseRepository.add(employeeCase);
        auditingService.audit("EmployeeCase", caseId, AuditOperation.CLOSED, "by " + userId);
    }

    @Override
    public EmployeeCase getCase(Long viewerId, Long caseId) {
        EmployeeCase employeeCase = caseRepository.requireById(caseId);
        boolean submitter = employeeCase.getSubmittedBy().getId().equals(viewerId);
        boolean assignee = employeeCase.getAssignedTo() != null && employeeCase.getAssignedTo().getId().equals(viewerId);
        boolean manager = accessControl.isManagerOf(viewerId, employeeCase.getSubmittedBy());
        boolean allowed = submitter || assignee || manager;
        if (employeeCase.getType() == CaseType.REQUEST)                   // cererile sunt vizibile si pentru HR
            allowed = allowed || accessControl.hasAnyRole(viewerId, Role.HR_EMPLOYEE_RELATIONS, Role.HR_ADMIN);
        if (!allowed)
            throw new AccessDeniedBusinessException("This case is not visible to the employee " + viewerId + "!");
        return employeeCase;
    }
}
