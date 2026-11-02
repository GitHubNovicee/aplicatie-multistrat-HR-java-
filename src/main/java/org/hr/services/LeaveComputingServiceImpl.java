package org.hr.services;

import org.hr.domain.common.DateRange;
import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveBalanceView;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveType;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class LeaveComputingServiceImpl implements ILeaveComputingService {
    private final IHolidayCalendar holidayCalendar;

    public LeaveComputingServiceImpl(IHolidayCalendar holidayCalendar) {
        this.holidayCalendar = holidayCalendar;
    }

    @Override
    public boolean isBalanceTracked(LeaveType type) {
        return type == LeaveType.ANNUAL;
    }

    @Override
    public int countWorkingDays(DateRange period) {
        if (period == null || period.getStartDate() == null || period.getEndDate() == null)
            return 0;
        int days = 0;
        for (LocalDate day = period.getStartDate(); !day.isAfter(period.getEndDate()); day = day.plusDays(1)) {
            boolean weekend = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
            if (!weekend && !holidayCalendar.isHoliday(day))
                days++;
        }
        return days;
    }

    @Override
    public LeaveRequest computeWorkingDays(LeaveRequest request) {
        request.setWorkingDays(countWorkingDays(request.getPeriod()));
        return request;
    }

    @Override
    public LeaveBalance computeRemainingDays(LeaveBalance balance) {
        balance.setRemainingDays(balance.getEntitledDays() - balance.getUsedDays());
        return balance;
    }

    @Override
    public LeaveBalance applyApproval(LeaveBalance balance, LeaveRequest request) {
        balance.setUsedDays(balance.getUsedDays() + request.getWorkingDays());
        return computeRemainingDays(balance);
    }

    @Override
    public LeaveBalance applyCancellation(LeaveBalance balance, LeaveRequest request) {
        int used = balance.getUsedDays() - request.getWorkingDays();
        if (used < 0)
            throw new IllegalStateException("Used days of the balance would become negative!");
        balance.setUsedDays(used);
        return computeRemainingDays(balance);
    }

    @Override
    public LeaveBalanceView toView(LeaveBalance balance) {
        computeRemainingDays(balance);
        return new LeaveBalanceView(
                balance.getEmployee().getEmployeeCode(),
                balance.getEmployee().getFullName(),
                balance.getType(), balance.getYear(),
                balance.getEntitledDays(), balance.getUsedDays(), balance.getRemainingDays());
    }
}
