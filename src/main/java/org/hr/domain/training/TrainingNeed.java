package org.hr.domain.training;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

/** RQ-08: nevoie de training identificata pentru un angajat. */
@Getter @Setter @NoArgsConstructor
public class TrainingNeed extends BaseEntity {
    @NotNull(message = "Employee is required!")
    private Employee employee;
    @NotNull(message = "Competency is required!")
    private Competency competency;
    private ProficiencyLevel targetLevel;
    @Size(max = 1000)
    private String justification;
    private Course suggestedCourse;
    @NotNull
    private TrainingNeedStatus status = TrainingNeedStatus.IDENTIFIED;
}
