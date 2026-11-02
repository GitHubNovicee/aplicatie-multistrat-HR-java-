package org.hr.domain.organization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.common.ContactInfo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

/**
 * Entitate radacina (RQ-01). Nu se sterge fizic, ci se arhiveaza logic (archived, archivedAt) - VR-28.
 * Campul calculat {@code seniority} (CR-08) este completat de IEmployeeComputingService.
 */
@Getter @Setter @NoArgsConstructor
public class Employee extends BaseEntity {
    @NotBlank(message = "Employee code is required!")
    @Size(max = 20, message = "Employee code is too long!")
    private String employeeCode;
    @NotBlank(message = "First name is required!")
    @Size(max = 100)
    private String firstName;
    @NotBlank(message = "Last name is required!")
    @Size(max = 100)
    private String lastName;
    @NotNull(message = "Contact info is required!")
    @Valid
    private ContactInfo contactInfo;
    @Past(message = "Birth date must be in the past!")
    private LocalDate birthDate;
    @NotNull(message = "Hire date is required!")
    private LocalDate hireDate;
    private LocalDate terminationDate;
    @NotNull(message = "Department is required!")
    private Department department;
    @NotNull(message = "Job position is required!")
    private JobPosition jobPosition;
    private Employee manager;
    /** arhivare logica - niciodata stergere fizica */
    private boolean archived = false;
    private Instant archivedAt;
    /** computed field (CR-08) */
    private Period seniority;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @AssertTrue(message = "Termination date must not be before hire date!")
    public boolean isTerminationDateValid() {
        return terminationDate == null || hireDate == null || !terminationDate.isBefore(hireDate);
    }
}
