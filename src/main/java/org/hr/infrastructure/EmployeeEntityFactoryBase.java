package org.hr.infrastructure;

import org.hr.domain.common.ContactInfo;
import org.hr.domain.organization.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class EmployeeEntityFactoryBase implements IEmployeeEntityFactory {
    private final IEmployeeEntityRepository employeeRepository;

    public EmployeeEntityFactoryBase(IEmployeeEntityRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Department buildDepartment(String name, Department parent) {
        Department department = new Department(name, parent);
        return department;
    }

    @Override
    public JobPosition buildJobPosition(String title, String grade) {
        JobPosition position = new JobPosition(title, grade);
        return position;
    }

    @Override
    public Employee buildEmployee(String firstName, String lastName, String email, String phone,
                                  LocalDate hireDate, Department department, JobPosition jobPosition, Employee manager) {
        Employee employee = new Employee();
        // codul de angajat este secvential (angajatii nu se sterg fizic, deci numaratoarea este monotona)
        employee.setEmployeeCode(String.format("EMP-%04d", employeeRepository.count() + 1));
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
        account.setUsername(username);
        account.setPasswordHash(passwordHash);
        account.setEmployee(employee);
        account.setRoles(roles == null ? new HashSet<>() : new HashSet<>(roles));
        return account;
    }
}
