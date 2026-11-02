package org.hr.services;

import jakarta.validation.Validator;
import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveStatus;
import org.hr.infrastructure.ILeaveBalanceEntityRepository;
import org.hr.infrastructure.ILeaveRequestEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Service
public class LeaveValidatingServiceImpl extends ValidatingServiceSupport implements ILeaveValidatingService {
    private final ILeaveBalanceEntityRepository balanceRepository;
    private final ILeaveRequestEntityRepository requestRepository;
    private final ILeaveComputingService computingService;
    private final IEmployeeValidatingService employeeValidatingService;

    public LeaveValidatingServiceImpl(Validator validator,
                                      ILeaveBalanceEntityRepository balanceRepository,
                                      ILeaveRequestEntityRepository requestRepository,
                                      ILeaveComputingService computingService,
                                      IEmployeeValidatingService employeeValidatingService) {
        super(validator);
        this.balanceRepository = balanceRepository;
        this.requestRepository = requestRepository;
        this.computingService = computingService;
        this.employeeValidatingService = employeeValidatingService;
    }

    /** Soldul aplicabil unei cereri (angajat, tip, anul datei de start). */
    private Optional<LeaveBalance> balanceOf(LeaveRequest request) {
        return balanceRepository.findFirst(b -> b.getEmployee().equals(request.getEmployee())
                && b.getType() == request.getType()
                && b.getYear() == request.getPeriod().getStartDate().getYear());
    }

    @Override
    public Set<String> validate(LeaveRequest request) {
        Set<String> violations = new TreeSet<>(beanViolations(request));
        if (!violations.isEmpty())
            return violations;     // regulile urmatoare presupun o cerere structural valida

        // VR-28
        if (request.getEmployee().isArchived())
            violations.add("VR-28: Archived employee " + request.getEmployee().getEmployeeCode()
                    + " cannot submit leave requests!");

        // exista cel putin o zi lucratoare
        if (request.getWorkingDays() <= 0)
            violations.add("period: The leave period contains no working days!");

        // VR-25: fara suprapunere cu alta cerere APPROVED/PENDING a aceluiasi angajat
        boolean overlaps = requestRepository.findFirst(r -> r.getEmployee().equals(request.getEmployee())
                && !r.equals(request)
                && (r.getStatus() == LeaveStatus.APPROVED || r.getStatus() == LeaveStatus.PENDING)
                && r.getPeriod().overlaps(request.getPeriod())).isPresent();
        if (overlaps)
            violations.add("VR-25: The leave period overlaps another APPROVED/PENDING request of the employee!");

        // VR-26: sold suficient pentru tipurile cu sold
        if (computingService.isBalanceTracked(request.getType())) {
            if (request.getPeriod().getStartDate().getYear() != request.getPeriod().getEndDate().getYear())
                violations.add("period: A leave with balance cannot span two calendar years - split the request!");
            else {
                Optional<LeaveBalance> balance = balanceOf(request);
                if (balance.isEmpty())
                    violations.add("VR-26: No " + request.getType() + " leave balance exists for "
                            + request.getPeriod().getStartDate().getYear() + "!");
                else if (computingService.computeRemainingDays(balance.get()).getRemainingDays() < request.getWorkingDays())
                    violations.add("VR-26: Insufficient leave balance (remaining "
                            + balance.get().getRemainingDays() + ", requested " + request.getWorkingDays() + ")!");
            }
        }
        return violations;
    }

    @Override
    public void validateLeaveRequest(LeaveRequest request) {
        throwIfAny(validate(request));
    }

    @Override
    public void validateApproval(LeaveRequest request) {
        Set<String> violations = new TreeSet<>();
        if (request.getStatus() != LeaveStatus.PENDING)
            violations.add("status: Only PENDING requests can be decided!");
        if (computingService.isBalanceTracked(request.getType())) {
            Optional<LeaveBalance> balance = balanceOf(request);
            if (balance.isEmpty())
                violations.add("VR-26: No leave balance exists for the request year!");
            else if (computingService.computeRemainingDays(balance.get()).getRemainingDays() < request.getWorkingDays())
                violations.add("VR-26: Insufficient leave balance (remaining "
                        + balance.get().getRemainingDays() + ", requested " + request.getWorkingDays() + ")!");
        }
        throwIfAny(violations);
    }

    @Override
    public void validateBalance(LeaveBalance balance) {
        Set<String> violations = new TreeSet<>(beanViolations(balance));
        if (balance.getEmployee() != null && balance.getType() != null
                && balanceRepository.findFirst(b -> b.getEmployee().equals(balance.getEmployee())
                && b.getType() == balance.getType() && b.getYear() == balance.getYear()
                && !b.equals(balance)).isPresent())
            violations.add("VR-24: A balance already exists for this employee, leave type and year!");
        if (balance.getUsedDays() > balance.getEntitledDays())
            violations.add("usedDays: Used days cannot exceed the entitled days!");
        throwIfAny(violations);
    }
}
