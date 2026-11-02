package org.hr.domain.organization;

import lombok.Value;

import java.time.Period;

/** Domain Entity View: fisa sintetica a angajatului (include vechimea - CR-08). */
@Value
public class EmployeeProfileView {
    String employeeCode;
    String fullName;
    String departmentName;
    String jobTitle;
    String managerName;
    boolean archived;
    Period seniority;
}
