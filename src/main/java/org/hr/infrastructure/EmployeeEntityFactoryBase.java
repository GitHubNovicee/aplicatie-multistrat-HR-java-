package org.hr.infrastructure;

import org.hr.domain.common.ContactInfo;
import org.hr.domain.organization.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class EmployeeEntityFactoryBase implements IEmployeeEntityFactory {
    private final IDepartmentEntityRepository departmentRepository;
    private final IJobPositionEntityRepository jobPositionRepository;
    private final IEmployeeEntityRepository employeeRepository;
    private final IUserAccountEntityRepository accountRepository;

    public EmployeeEntityFactoryBase(IDepartmentEntityRepository departmentRepository,
                                     IJobPositionEntityRepository jobPositionRepository,
                                     IEmployeeEntityRepository employeeRepository,
                                     IUserAccountEntityRepository accountRepository) {
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Department buildDepartment(String name, Department parent) {
        Department department = new Department(name, parent);
        department.setId(departmentRepository.getNextID());
        return department;
    }

    @Override
    public JobPosition buildJobPosition(String title, String grade) {
        JobPosition position = new JobPosition(title, grade);
        position.setId(jobPositionRepository.getNextID());
        return position;
    }

    @Override
    public Employee buildEmployee(String firstName, String lastName, String email, String phone,
                                  LocalDate hireDate, Department department, JobPosition jobPosition, Employee manager) {
        Employee employee = new Employee();
        employee.setId(employeeRepository.getNextID());
        employee.setEmployeeCode(String.format("EMP-%04d", employee.getId()));
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setContactInfo(ContactInfo.of(email, phone));
        employee.setHireDate(hireDate);
        employee.setDepartment(department);
        employee.setJobPosition(jobPosition);
        employee.setManager(manager);
        return employee;
    }

    @Override
    public UserAccount buildUserAccount(Employee employee, String username, String passwordHash, Set<Role> roles) {
        UserAccount account = new UserAccount();
        account.setId(accountRepository.getNextID());
        account.setUsername(username);
        account.setPasswordHash(passwordHash);
        account.setEmployee(employee);
        account.setRoles(roles == null ? new HashSet<>() : new HashSet<>(roles));
        return account;
    }
}
