package org.hr.application;

import org.hr.domain.leave.LeaveBalanceView;
import org.hr.domain.leave.LeaveStatus;
import org.hr.domain.leave.LeaveType;

import java.time.LocalDate;

/*
 * Business Workflow (RQ-06): solduri anuale, cereri cu aprobare, calcul zile lucratoare. UC-13 .. UC-16.
 */
public interface ILeaveWorkflowService {
    /** Acordare sold anual (HR_ADMIN); VR-24. */
    Long grantLeaveBalance(Long adminId, Long employeeId, LeaveType type, int year, int entitledDays);

    /** UC-13: cerere PENDING cu workingDays calculat (CR-02); VR-25, VR-26, VR-28; notifica managerul. */
    Long requestLeave(Long employeeId, LeaveType type, LocalDate startDate, LocalDate endDate, String reason);

    /** UC-14 (managerul angajatului sau HR_ADMIN): APPROVED / REJECTED; la aprobare usedDays += workingDays (CR-03). */
    LeaveStatus decideLeave(Long deciderId, Long requestId, boolean approve, String note);

    /** UC-15 (angajatul): CANCELLED; daca era APPROVED soldul este restituit (CR-03). */
    LeaveStatus cancelLeave(Long employeeId, Long requestId);

    /** UC-16: zile ramase (CR-01). */
    LeaveBalanceView getLeaveBalance(Long employeeId, LeaveType type, int year);
}
