package org.hr.infrastructure;

import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveType;
import org.hr.domain.organization.Employee;

import java.time.LocalDate;

/** Factory pentru modulul Concedii (RQ-06). */
public interface ILeaveEntityFactory {
    LeaveBalance buildLeaveBalance(Employee employee, LeaveType type, int year, int entitledDays);

    /** Cerere PENDING; workingDays se calculeaza ulterior (CR-02). */
    LeaveRequest buildLeaveRequest(Employee employee, LeaveType type, LocalDate startDate, LocalDate endDate, String reason);
}
