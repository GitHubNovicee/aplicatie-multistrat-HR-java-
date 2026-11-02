package org.hr.domain.development;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.common.DateRange;
import org.hr.domain.organization.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Radacina agregatului DevelopmentPlan (RQ-09): obiective, termene si rezultate. */
@Getter @Setter @NoArgsConstructor
public class DevelopmentPlan extends BaseEntity {
    @NotNull(message = "Employee is required!")
    private Employee employee;
    @NotBlank(message = "Plan title is required!")
    @Size(max = 200)
    private String title;
    @Size(max = 2000)
    private String description;
    @NotNull(message = "Plan period is required!")
    @Valid
    private DateRange period;
    @Valid
    private List<Objective> objectives = new ArrayList<>();
    @NotNull
    private PlanStatus status = PlanStatus.DRAFT;
    /** computed field (CR-09): procent obiective ACHIEVED / obiective neanulate */
    private Double progress;

    public Objective addObjective(Objective objective) {
        return addComponent(objectives, objective);
    }

    /** orphanRemoval: obiectivul dispare odata cu eliminarea din agregat. */
    public boolean removeObjective(Objective objective) {
        return objectives.remove(objective);
    }

    public Optional<Objective> findObjective(Long objectiveId) {
        return objectives.stream().filter(o -> o.getId().equals(objectiveId)).findFirst();
    }
}
