package org.hr.application;

import org.hr.domain.organization.EmployeeProfileView;
import org.hr.domain.organization.Role;

import java.time.LocalDate;
import java.util.Set;

/*
 * Business Workflow Steps/Actions (UC-01, UC-02, UC-03) - actor: HR_ADMIN
 * (1) create departments / job positions   (2) create employee   (3) assign department head
 * (4) create user account with roles       (5) archive employee (logical)   (6) enable/disable account
 */
public interface IOrganizationWorkflowService {
    Long createDepartment(Long adminId, String name, Long parentDepartmentId);

    Long createJobPosition(Long adminId, String title, String grade);

    Long createEmployee(Long adminId, String firstName, String lastName, String email, String phone,
                        LocalDate hireDate, Long departmentId, Long jobPositionId, Long managerId);

    void assignDepartmentHead(Long adminId, Long departmentId, Long employeeId);

    /** UC-01 / VR-28: arhivare logica, niciodata stergere fizica; contul este dezactivat. */
    EmployeeProfileView archiveEmployee(Long adminId, Long employeeId, LocalDate terminationDate);

    EmployeeProfileView getEmployeeProfile(Long employeeId);

    Long createUserAccount(Long adminId, Long employeeId, String username, String passwordHash, Set<Role> roles);

    void changeAccountRoles(Long adminId, Long accountId, Set<Role> roles);

    void setAccountEnabled(Long adminId, Long accountId, boolean enabled);
}
