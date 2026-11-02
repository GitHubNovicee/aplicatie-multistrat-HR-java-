package org.hr.services;

import jakarta.validation.Validator;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.UserAccount;
import org.hr.infrastructure.IEmployeeEntityRepository;
import org.hr.infrastructure.IUserAccountEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

@Service
public class EmployeeValidatingServiceImpl extends ValidatingServiceSupport implements IEmployeeValidatingService {
    private static final Logger logger = Logger.getLogger(EmployeeValidatingServiceImpl.class.getName());

    private final IEmployeeEntityRepository employeeRepository;
    private final IUserAccountEntityRepository accountRepository;

    public EmployeeValidatingServiceImpl(Validator validator,
                                         IEmployeeEntityRepository employeeRepository,
                                         IUserAccountEntityRepository accountRepository) {
        super(validator);
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Set<String> validate(Employee employee) {
        Set<String> violations = new TreeSet<>(beanViolations(employee));
        // RQ-01: cod unic si email unic
        if (employee.getEmployeeCode() != null && employeeRepository.findFirst(e ->
                employee.getEmployeeCode().equalsIgnoreCase(e.getEmployeeCode()) && !e.equals(employee)).isPresent())
            violations.add("employeeCode: Employee code '" + employee.getEmployeeCode() + "' is already used!");
        if (employee.getContactInfo() != null && employee.getContactInfo().getEmail() != null
                && employeeRepository.findFirst(e -> e.getContactInfo().getEmail()
                .equalsIgnoreCase(employee.getContactInfo().getEmail()) && !e.equals(employee)).isPresent())
            violations.add("contactInfo.email: Email '" + employee.getContactInfo().getEmail() + "' is already used!");
        // RQ-01: managerul direct
        if (employee.getManager() != null) {
            if (employee.getManager().equals(employee))
                violations.add("manager: An employee cannot be his own manager!");
            else if (employee.getManager().isArchived())
                violations.add("manager: The direct manager is archived!");
        }
        logger.info("Employee violations count: " + violations.size());
        return violations;
    }

    @Override
    public void validateWithException(Employee employee) {
        throwIfAny(validate(employee));
    }

    @Override
    public void validateEmployeeIsActive(Employee employee) {
        if (employee.isArchived())
            throwIfAny(Set.of("VR-28: Archived employee " + employee.getEmployeeCode()
                    + " cannot submit requests or receive new sessions!"));
    }

    @Override
    public Set<String> validate(UserAccount account) {
        Set<String> violations = new TreeSet<>(beanViolations(account));
        if (account.getUsername() != null && accountRepository.findFirst(a ->
                a.getUsername().equalsIgnoreCase(account.getUsername()) && !a.equals(account)).isPresent())
            violations.add("username: Username '" + account.getUsername() + "' is already used!");
        // RQ-03: un singur cont per angajat
        if (account.getEmployee() != null && accountRepository.findFirst(a ->
                a.getEmployee().equals(account.getEmployee()) && !a.equals(account)).isPresent())
            violations.add("employee: The employee already has a user account!");
        return violations;
    }

    @Override
    public void validateWithException(UserAccount account) {
        throwIfAny(validate(account));
    }
}
