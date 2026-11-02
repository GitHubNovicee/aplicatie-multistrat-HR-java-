package org.hr.application;

import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.common.EntityNotFoundException;
import org.hr.domain.common.HrAuditView.AuditOperation;
import org.hr.domain.leave.*;
import org.hr.domain.notification.NotificationType;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.infrastructure.*;
import org.hr.services.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Service
public class LeaveWorkflowServiceImpl implements ILeaveWorkflowService {
    private final IEmployeeEntityRepository employeeRepository;
    private final ILeaveBalanceEntityRepository balanceRepository;
    private final ILeaveRequestEntityRepository requestRepository;
    private final ILeaveEntityFactory factory;
    private final ILeaveValidatingService validator;
    private final ILeaveComputingService computingService;
    private final IAccessControlService accessControl;
    private final INotificationService notificationService;
    private final IHrAuditingService auditingService;
    private final Clock clock;

    public LeaveWorkflowServiceImpl(IEmployeeEntityRepository employeeRepository,
                                    ILeaveBalanceEntityRepository balanceRepository,
                                    ILeaveRequestEntityRepository requestRepository,
                                    ILeaveEntityFactory factory, ILeaveValidatingService validator,
                                    ILeaveComputingService computingService, IAccessControlService accessControl,
                                    INotificationService notificationService,
                                    IHrAuditingService auditingService, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.balanceRepository = balanceRepository;
        this.requestRepository = requestRepository;
        this.factory = factory;
        this.validator = validator;
        this.computingService = computingService;
        this.accessControl = accessControl;
        this.notificationService = notificationService;
        this.auditingService = auditingService;
        this.clock = clock;
    }

    private LeaveBalance balanceOf(Employee employee, LeaveType type, int year) {
        return balanceRepository.findFirst(b -> b.getEmployee().equals(employee) && b.getType() == type
                        && b.getYear() == year)
                .orElseThrow(() -> new EntityNotFoundException("LeaveBalance",
                        employee.getEmployeeCode() + "/" + type + "/" + year));
    }

    @Override
    public Long grantLeaveBalance(Long adminId, Long employeeId, LeaveType type, int year, int entitledDays) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        LeaveBalance balance = factory.buildLeaveBalance(employeeRepository.requireById(employeeId), type, year, entitledDays);
        validator.validateBalance(balance);                               // VR-24
        balanceRepository.add(balance);
        auditingService.audit("LeaveBalance", balance.getId(), AuditOperation.CREATED, type + "/" + year);
        return balance.getId();
    }

    @Override
    public Long requestLeave(Long employeeId, LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        Employee employee = employeeRepository.requireById(employeeId);
        LeaveRequest request = factory.buildLeaveRequest(employee, type, startDate, endDate, reason);
        computingService.computeWorkingDays(request);                     // CR-02
        validator.validateLeaveRequest(request);                          // VR-25, VR-26, VR-28
        requestRepository.add(request);
        notificationService.notifyEmployee(employee.getManager(), NotificationType.APPROVAL,
                employee.getFullName() + " requested leave " + startDate + " - " + endDate, "LEAVE_REQUEST", request.getId());
        auditingService.audit("LeaveRequest", request.getId(), AuditOperation.SUBMITTED, request.getWorkingDays() + " working days");
        return request.getId();
    }

    @Override
    public LeaveStatus decideLeave(Long deciderId, Long requestId, boolean approve, String note) {
        LeaveRequest request = requestRepository.requireById(requestId);
        Employee decider = employeeRepository.requireById(deciderId);
        if (decider.equals(request.getEmployee()))
            throw new AccessDeniedBusinessException("An employee cannot decide his own leave request!");
        accessControl.requireManagerOfOrAnyRole(deciderId, request.getEmployee(), Role.HR_ADMIN);
        if (approve) {
            validator.validateApproval(request);                          // VR-26
            if (computingService.isBalanceTracked(request.getType())) {
                LeaveBalance balance = balanceOf(request.getEmployee(), request.getType(),
                        request.getPeriod().getStartDate().getYear());
                balanceRepository.add(computingService.applyApproval(balance, request));   // CR-03
            }
        } else if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessValidationException("Only PENDING requests can be decided!");
        }
        request.setStatus(approve ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        request.setDecidedBy(decider);
        request.setDecidedAt(Instant.now(clock));
        request.setDecisionNote(note);
        requestRepository.add(request);
        notificationService.notifyEmployee(request.getEmployee(), NotificationType.APPROVAL,
                "Your leave request #" + requestId + " was " + request.getStatus(), "LEAVE_REQUEST", requestId);
        auditingService.audit("LeaveRequest", requestId, approve ? AuditOperation.APPROVED : AuditOperation.REJECTED, "by " + deciderId);
        return request.getStatus();
    }

    @Override
    public LeaveStatus cancelLeave(Long employeeId, Long requestId) {
        LeaveRequest request = requestRepository.requireById(requestId);
        if (!request.getEmployee().getId().equals(employeeId))
            throw new AccessDeniedBusinessException("Only the requesting employee can cancel the leave!");
        if (request.getStatus() != LeaveStatus.PENDING && request.getStatus() != LeaveStatus.APPROVED)
            throw new BusinessValidationException("Only PENDING or APPROVED requests can be cancelled!");
        if (request.getStatus() == LeaveStatus.APPROVED && computingService.isBalanceTracked(request.getType())) {
            LeaveBalance balance = balanceOf(request.getEmployee(), request.getType(),
                    request.getPeriod().getStartDate().getYear());
            balanceRepository.add(computingService.applyCancellation(balance, request));   // CR-03
        }
        request.setStatus(LeaveStatus.CANCELLED);
        requestRepository.add(request);
        auditingService.audit("LeaveRequest", requestId, AuditOperation.CANCELLED, "by employee");
        return request.getStatus();
    }

    @Override
    public LeaveBalanceView getLeaveBalance(Long employeeId, LeaveType type, int year) {
        return computingService.toView(balanceOf(employeeRepository.requireById(employeeId), type, year));
    }
}
