package org.hr.infrastructure;

import org.hr.domain.common.DateRange;
import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveStatus;
import org.hr.domain.leave.LeaveType;
import org.hr.domain.organization.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LeaveEntityFactoryBase implements ILeaveEntityFactory {
    private final ILeaveBalanceEntityRepository balanceRepository;
    private final ILeaveRequestEntityRepository requestRepository;

    public LeaveEntityFactoryBase(ILeaveBalanceEntityRepository balanceRepository,
                                  ILeaveRequestEntityRepository requestRepository) {
        this.balanceRepository = balanceRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public LeaveBalance buildLeaveBalance(Employee employee, LeaveType type, int year, int entitledDays) {
        LeaveBalance balance = new LeaveBalance();
        balance.setId(balanceRepository.getNextID());
        balance.setEmployee(employee);
        balance.setType(type);
        balance.setYear(year);
        balance.setEntitledDays(entitledDays);
        balance.setUsedDays(0);
        balance.setRemainingDays(entitledDays);
        return balance;
    }

    @Override
    public LeaveRequest buildLeaveRequest(Employee employee, LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        LeaveRequest request = new LeaveRequest();
        request.setId(requestRepository.getNextID());
        request.setEmployee(employee);
        request.setType(type);
        request.setPeriod(DateRange.of(startDate, endDate));
        request.setReason(reason);
        request.setStatus(LeaveStatus.PENDING);
        return request;
    }
}
