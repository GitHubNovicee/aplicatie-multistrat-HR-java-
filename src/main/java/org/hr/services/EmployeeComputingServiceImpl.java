package org.hr.services;

import org.hr.domain.organization.Employee;
import org.hr.domain.organization.EmployeeProfileView;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeComputingServiceImpl implements IEmployeeComputingService {
    private final Clock clock;

    public EmployeeComputingServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Period computeSeniority(Employee employee) {
        LocalDate end = employee.getTerminationDate() != null ? employee.getTerminationDate() : LocalDate.now(clock);
        if (employee.getHireDate() == null || employee.getHireDate().isAfter(end))
            return Period.ZERO;
        return Period.between(employee.getHireDate(), end);
    }

    @Override
    public Employee computeSeniorityOf(Employee employee) {
        employee.setSeniority(computeSeniority(employee));
        return employee;
    }

    @Override
    public EmployeeProfileView toProfileView(Employee employee) {
        computeSeniorityOf(employee);
        return new EmployeeProfileView(
                employee.getEmployeeCode(), employee.getFullName(),
                employee.getDepartment().getName(), employee.getJobPosition().getTitle(),
                employee.getManager() == null ? null : employee.getManager().getFullName(),
                employee.isArchived(), employee.getSeniority());
    }

    @Override
    public List<EmployeeProfileView> toProfileView(Collection<Employee> employees) {
        return employees.stream().map(this::toProfileView).collect(Collectors.toList());
    }
}
