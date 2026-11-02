package org.hr.services;

import org.hr.domain.common.DateRange;
import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveBalanceView;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveType;

/** Domain Computation Service (CR-01, CR-02, CR-03) - modulul Concedii. */
public interface ILeaveComputingService {
    /** Tipurile cu sold (VR-26): doar ANNUAL. */
    boolean isBalanceTracked(LeaveType type);

    /** CR-02: zile lucratoare intre startDate si endDate inclusiv, fara weekenduri si sarbatori legale. */
    int countWorkingDays(DateRange period);

    /** CR-02: completeaza campul calculat LeaveRequest.workingDays. */
    LeaveRequest computeWorkingDays(LeaveRequest request);

    /** CR-01: remainingDays = entitledDays - usedDays. */
    LeaveBalance computeRemainingDays(LeaveBalance balance);

    /** CR-03: la aprobare usedDays += workingDays. */
    LeaveBalance applyApproval(LeaveBalance balance, LeaveRequest request);

    /** CR-03: la anulare dupa aprobare usedDays -= workingDays. */
    LeaveBalance applyCancellation(LeaveBalance balance, LeaveRequest request);

    /** Domain Entity View (UC-16). */
    LeaveBalanceView toView(LeaveBalance balance);
}
