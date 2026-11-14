package org.hr.domain.training;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

/** RQ-08: nevoie de training identificata pentru un angajat. */
@Entity
@Getter @Setter @NoArgsConstructor
public class TrainingNeed extends BaseEntity {
    @NotNull(message = "Employee is required!")
    @ManyToOne(optional = false)
    private Employee employee;
    @NotNull(message = "Competency is required!")
    @ManyToOne(optional = false)
    private Competency competency;
    @Enumerated(EnumType.STRING)
    private ProficiencyLevel targetLevel;
    @Size(max = 1000)
    @Column(length = 1000)
    private String justification;
    @ManyToOne(optional = true)
    private Course suggestedCourse;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingNeedStatus status = TrainingNeedStatus.IDENTIFIED;
}
