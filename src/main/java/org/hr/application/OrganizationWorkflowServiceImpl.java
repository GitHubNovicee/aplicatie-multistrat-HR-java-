package org.hr.application;

import org.hr.domain.common.HrAuditView.AuditOperation;
import org.hr.domain.organization.*;
import org.hr.infrastructure.*;
import org.hr.services.IAccessControlService;
import org.hr.services.IEmployeeComputingService;
import org.hr.services.IEmployeeValidatingService;
import org.hr.services.IHrAuditingService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class OrganizationWorkflowServiceImpl implements IOrganizationWorkflowService {
    private final IEmployeeEntityFactory factory;
    private final IDepartmentEntityRepository departmentRepository;
    private final IJobPositionEntityRepository jobPositionRepository;
    private final IEmployeeEntityRepository employeeRepository;
    private final IUserAccountEntityRepository accountRepository;
    private final IEmployeeValidatingService validator;
    private final IEmployeeComputingService computingService;
    private final IAccessControlService accessControl;
    private final IHrAuditingService auditingService;
    private final Clock clock;

    public OrganizationWorkflowServiceImpl(IEmployeeEntityFactory factory,
                                           IDepartmentEntityRepository departmentRepository,
                                           IJobPositionEntityRepository jobPositionRepository,
                                           IEmployeeEntityRepository employeeRepository,
                                           IUserAccountEntityRepository accountRepository,
                                           IEmployeeValidatingService validator,
                                           IEmployeeComputingService computingService,
                                           IAccessControlService accessControl,
                                           IHrAuditingService auditingService, Clock clock) {
        this.factory = factory;
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.validator = validator;
        this.computingService = computingService;
        this.accessControl = accessControl;
        this.auditingService = auditingService;
        this.clock = clock;
    }

    @Override
    public Long createDepartment(Long adminId, String name, Long parentDepartmentId) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        Department parent = parentDepartmentId == null ? null : departmentRepository.requireById(parentDepartmentId);
        Department department = factory.buildDepartment(name, parent);
        departmentRepository.add(department);
        auditingService.audit("Department", department.getId(), AuditOperation.CREATED, name);
        return department.getId();
    }

    @Override
    public Long createJobPosition(Long adminId, String title, String grade) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        JobPosition position = factory.buildJobPosition(title, grade);
        jobPositionRepository.add(position);
        auditingService.audit("JobPosition", position.getId(), AuditOperation.CREATED, title);
        return position.getId();
    }

    @Override
    public Long createEmployee(Long adminId, String firstName, String lastName, String email, String phone,
                               LocalDate hireDate, Long departmentId, Long jobPositionId, Long managerId) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        Employee employee = factory.buildEmployee(firstName, lastName, email, phone, hireDate,
                departmentRepository.requireById(departmentId), jobPositionRepository.requireById(jobPositionId),
                managerId == null ? null : employeeRepository.requireById(managerId));
        validator.validateWithException(employee);
        employeeRepository.add(employee);
        auditingService.audit("Employee", employee.getId(), AuditOperation.CREATED, employee.getEmployeeCode());
        return employee.getId();
    }

    @Override
    public void assignDepartmentHead(Long adminId, Long departmentId, Long employeeId) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        Department department = departmentRepository.requireById(departmentId);
        Employee head = employeeRepository.requireById(employeeId);
        validator.validateEmployeeIsActive(head);
        department.setHead(head);
        departmentRepository.add(department);
        auditingService.audit("Department", departmentId, AuditOperation.UPDATED, "head=" + head.getEmployeeCode());
    }

    @Override
    public EmployeeProfileView archiveEmployee(Long adminId, Long employeeId, LocalDate terminationDate) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        Employee employee = employeeRepository.requireById(employeeId);
        employee.setArchived(true);
        employee.setArchivedAt(Instant.now(clock));
        employee.setTerminationDate(terminationDate == null ? LocalDate.now(clock) : terminationDate);
        validator.validateWithException(employee);
        employeeRepository.add(employee);
        accountRepository.find(a -> a.getEmployee().equals(employee)).forEach(a -> {
            a.setEnabled(false);
            accountRepository.add(a);
        });
        auditingService.audit("Employee", employeeId, AuditOperation.ARCHIVED, employee.getEmployeeCode());
        return computingService.toProfileView(employee);
    }

    @Override
    public EmployeeProfileView getEmployeeProfile(Long employeeId) {
        return computingService.toProfileView(employeeRepository.requireById(employeeId));
    }

    @Override
    public Long createUserAccount(Long adminId, Long employeeId, String username, String passwordHash, Set<Role> roles) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        Employee employee = employeeRepository.requireById(employeeId);
        validator.validateEmployeeIsActive(employee);
        UserAccount account = factory.buildUserAccount(employee, username, passwordHash, roles);
        validator.validateWithException(account);
        accountRepository.add(account);
        auditingService.audit("UserAccount", account.getId(), AuditOperation.CREATED, username);
        return account.getId();
    }

    @Override
    public void changeAccountRoles(Long adminId, Long accountId, Set<Role> roles) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        UserAccount account = accountRepository.requireById(accountId);
        account.setRoles(new HashSet<>(roles));
        validator.validateWithException(account);
        accountRepository.add(account);
        auditingService.audit("UserAccount", accountId, AuditOperation.UPDATED, "roles=" + roles);
    }

    @Override
    public void setAccountEnabled(Long adminId, Long accountId, boolean enabled) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        UserAccount account = accountRepository.requireById(accountId);
        account.setEnabled(enabled);
        accountRepository.add(account);
        auditingService.audit("UserAccount", accountId, AuditOperation.UPDATED, "enabled=" + enabled);
    }
}
