package org.hr.domain.development;

import jakarta.persistence.*;
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
@Entity
@Getter @Setter @NoArgsConstructor
public class DevelopmentPlan extends BaseEntity {
    @NotNull(message = "Employee is required!")
    @ManyToOne(optional = false)
    private Employee employee;
    @NotBlank(message = "Plan title is required!")
    @Size(max = 200)
    @Column(nullable = false)
    private String title;
    @Size(max = 2000)
    @Column(length = 2000)
    private String description;
    @NotNull(message = "Plan period is required!")
    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "period_start", nullable = false)),
            @AttributeOverride(name = "endDate", column = @Column(name = "period_end", nullable = false))})
    private DateRange period;
    @Valid
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Objective> objectives = new ArrayList<>();
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.DRAFT;
    /** computed field (CR-09): procent obiective ACHIEVED / obiective neanulate */
    @Transient
    private Double progress;

    public Objective addObjective(Objective objective) {
        objective.setPlan(this);
        return addComponent(objectives, objective);
    }

    /** orphanRemoval: obiectivul dispare odata cu eliminarea din agregat. */
    public boolean removeObjective(Objective objective) {
        boolean removed = objectives.remove(objective);
        if (removed) objective.setPlan(null);
        return removed;
    }

    public Optional<Objective> findObjective(Long objectiveId) {
        return objectives.stream().filter(o -> java.util.Objects.equals(o.getId(), objectiveId)).findFirst();
    }
}
