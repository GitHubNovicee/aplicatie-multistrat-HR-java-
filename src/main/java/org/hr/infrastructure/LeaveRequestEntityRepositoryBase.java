package org.hr.infrastructure;

import org.hr.domain.leave.LeaveRequest;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class LeaveRequestEntityRepositoryBase extends InMemoryEntityRepositoryBase<LeaveRequest> implements ILeaveRequestEntityRepository {
    public LeaveRequestEntityRepositoryBase() {
        super("LeaveRequest");
    }
}
