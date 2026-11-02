package org.hr.infrastructure;

import org.hr.domain.leave.LeaveBalance;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class LeaveBalanceEntityRepositoryBase extends InMemoryEntityRepositoryBase<LeaveBalance> implements ILeaveBalanceEntityRepository {
    public LeaveBalanceEntityRepositoryBase() {
        super("LeaveBalance");
    }
}
