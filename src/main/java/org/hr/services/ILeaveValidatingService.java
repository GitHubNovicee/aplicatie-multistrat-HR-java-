package org.hr.services;

import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveRequest;

import java.util.Set;

/** Domain Validation Service - modulul Concedii (RQ-06, VR-24, VR-25, VR-26, VR-28). */
public interface ILeaveValidatingService {
    /** Violari pentru cerere (bean validation + VR-25 + VR-26 + VR-28); multime goala daca este valida. */
    Set<String> validate(LeaveRequest request);

    void validateLeaveRequest(LeaveRequest request);

    /** VR-26 la momentul aprobarii: soldul disponibil acopera workingDays. */
    void validateApproval(LeaveRequest request);

    /** VR-24: un sold unic per (angajat, tip, an). */
    void validateBalance(LeaveBalance balance);
}
