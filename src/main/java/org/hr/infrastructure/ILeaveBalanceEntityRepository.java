package org.hr.infrastructure;

import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveType;
import org.hr.domain.organization.Employee;

import java.util.List;
import java.util.Optional;

public interface ILeaveBalanceEntityRepository extends IEntityRepository<LeaveBalance> {
    Optional<LeaveBalance> findByEmployeeAndTypeAndYear(Employee employee, LeaveType type, int year);

    List<LeaveBalance> findByEmployee(Employee employee);
}
