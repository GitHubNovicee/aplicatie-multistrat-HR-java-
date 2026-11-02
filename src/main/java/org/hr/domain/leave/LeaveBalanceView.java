package org.hr.domain.leave;

import lombok.Value;

/** Domain Entity View (UC-16): zile ramase din sold. */
@Value
public class LeaveBalanceView {
    String employeeCode;
    String employeeName;
    LeaveType type;
    int year;
    int entitledDays;
    int usedDays;
    int remainingDays;
}
