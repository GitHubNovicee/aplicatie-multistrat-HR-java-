package org.hr.application;

import org.hr.HrTestSupport;
import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.common.DateRange;
import org.hr.domain.leave.*;
import org.hr.domain.organization.Role;
import org.hr.infrastructure.ILeaveRequestEntityRepository;
import org.hr.infrastructure.IHrSampleDataFactory;
import org.hr.services.ILeaveComputingService;
import org.hr.services.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** WORKFLOW Business Service Tests - concedii UC-13 .. UC-16 (CR-01, CR-02, CR-03, VR-24, VR-25, VR-26). */
public class Test4_LeaveWorkflowService extends HrTestSupport {
    @Autowired private ILeaveWorkflowService workflow;
    @Autowired private ILeaveComputingService computing;
    @Autowired private ILeaveRequestEntityRepository requestRepository;
    @Autowired private INotificationService notificationService;

    private Long manager() { return idOf(IHrSampleDataFactory.MANAGER_USERNAME); }

    private int workingDaysOfWeek(LocalDate monday) {
        return computing.countWorkingDays(DateRange.of(monday, monday.plusDays(4)));
    }

    @Test
    void requestApproveCancel_updatesBalance() {
        LocalDate monday = nextWorkWeekStart();
        int wd = workingDaysOfWeek(monday);
        Long emp = newEmployee(Role.EMPLOYEE);
        workflow.grantLeaveBalance(adminId(), emp, LeaveType.ANNUAL, monday.getYear(), 21);
        assertThrows(BusinessValidationException.class,                                   // VR-24
                () -> workflow.grantLeaveBalance(adminId(), emp, LeaveType.ANNUAL, monday.getYear(), 10));

        // UC-13: PENDING, workingDays calculat (CR-02), managerul este notificat
        Long requestId = workflow.requestLeave(emp, LeaveType.ANNUAL, monday, monday.plusDays(4), "vacation");
        LeaveRequest request = requestRepository.requireById(requestId);
        assertEquals(LeaveStatus.PENDING, request.getStatus());
        assertEquals(wd, request.getWorkingDays());
        assertTrue(notificationService.getNotificationsOf(manager()).stream()
                .anyMatch(n -> requestId.equals(n.getReferenceId()) && "LEAVE_REQUEST".equals(n.getReferenceType())));

        // VR-25: suprapunere cu o cerere PENDING
        BusinessValidationException overlap = assertThrows(BusinessValidationException.class,
                () -> workflow.requestLeave(emp, LeaveType.ANNUAL, monday.plusDays(2), monday.plusDays(3), "again"));
        assertTrue(overlap.getMessage().contains("VR-25"));

        // UC-14: aprobare de managerul angajatului; usedDays += workingDays (CR-03)
        assertEquals(LeaveStatus.APPROVED, workflow.decideLeave(manager(), requestId, true, "enjoy"));
        LeaveBalanceView afterApproval = workflow.getLeaveBalance(emp, LeaveType.ANNUAL, monday.getYear());
        assertEquals(wd, afterApproval.getUsedDays());
        assertEquals(21 - wd, afterApproval.getRemainingDays());                          // CR-01
        assertThrows(BusinessValidationException.class, () -> workflow.decideLeave(manager(), requestId, true, "twice"));

        // UC-15: anulare dupa aprobare -> sold restituit
        assertEquals(LeaveStatus.CANCELLED, workflow.cancelLeave(emp, requestId));
        assertEquals(21, workflow.getLeaveBalance(emp, LeaveType.ANNUAL, monday.getYear()).getRemainingDays());
        assertThrows(BusinessValidationException.class, () -> workflow.cancelLeave(emp, requestId));
    }

    @Test
    void rejectedRequestDoesNotTouchBalance() {
        LocalDate monday = nextWorkWeekStart();
        Long emp = newEmployee(Role.EMPLOYEE);
        workflow.grantLeaveBalance(adminId(), emp, LeaveType.ANNUAL, monday.getYear(), 21);
        Long requestId = workflow.requestLeave(emp, LeaveType.ANNUAL, monday, monday.plusDays(4), "x");
        assertEquals(LeaveStatus.REJECTED, workflow.decideLeave(manager(), requestId, false, "busy period"));
        assertEquals(0, workflow.getLeaveBalance(emp, LeaveType.ANNUAL, monday.getYear()).getUsedDays());
        // o cerere respinsa nu mai blocheaza perioada (VR-25 se aplica doar APPROVED/PENDING)
        assertDoesNotThrow(() -> workflow.requestLeave(emp, LeaveType.ANNUAL, monday, monday.plusDays(4), "retry"));
    }

    @Test
    void vr26_insufficientBalanceIsRejected() {
        LocalDate monday = nextWorkWeekStart();
        int wd = workingDaysOfWeek(monday);
        Long emp = newEmployee(Role.EMPLOYEE);
        workflow.grantLeaveBalance(adminId(), emp, LeaveType.ANNUAL, monday.getYear(), wd - 1);
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> workflow.requestLeave(emp, LeaveType.ANNUAL, monday, monday.plusDays(4), "too long"));
        assertTrue(ex.getMessage().contains("VR-26"));
    }

    @Test
    void unpaidLeaveHasNoBalanceCheck() {
        LocalDate monday = nextWorkWeekStart();
        Long emp = newEmployee(Role.EMPLOYEE);
        Long requestId = workflow.requestLeave(emp, LeaveType.UNPAID, monday, monday.plusDays(4), "x");
        assertEquals(LeaveStatus.APPROVED, workflow.decideLeave(manager(), requestId, true, null));
    }

    @Test
    void weekendOnlyPeriodIsRejected() {
        LocalDate saturday = nextWorkWeekStart().plusDays(5);
        Long emp = newEmployee(Role.EMPLOYEE);
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> workflow.requestLeave(emp, LeaveType.UNPAID, saturday, saturday.plusDays(1), "weekend"));
        assertTrue(ex.getMessage().contains("no working days"));
    }

    @Test
    void permissions_onlyTeamManagerOrAdminDecides_onlyOwnerCancels() {
        LocalDate monday = nextWorkWeekStart();
        Long emp = newEmployee(Role.EMPLOYEE);
        Long colleague = newEmployee(Role.EMPLOYEE);
        Long requestId = workflow.requestLeave(emp, LeaveType.UNPAID, monday, monday.plusDays(4), "x");
        assertThrows(AccessDeniedBusinessException.class, () -> workflow.decideLeave(colleague, requestId, true, null));
        assertThrows(AccessDeniedBusinessException.class, () -> workflow.decideLeave(emp, requestId, true, null));
        assertThrows(AccessDeniedBusinessException.class, () -> workflow.cancelLeave(colleague, requestId));
        assertEquals(LeaveStatus.APPROVED, workflow.decideLeave(adminId(), requestId, true, "hr decision"));
    }

    @Test
    void vr28_archivedEmployeeCannotRequestLeave() {
        LocalDate monday = nextWorkWeekStart();
        Long emp = newEmployee(Role.EMPLOYEE);
        organizationWorkflow.archiveEmployee(adminId(), emp, LocalDate.now());
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> workflow.requestLeave(emp, LeaveType.UNPAID, monday, monday.plusDays(4), "x"));
        assertTrue(ex.getMessage().contains("VR-28"));
    }
}
