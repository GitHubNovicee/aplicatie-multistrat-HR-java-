package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class DevelopmentPlan extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @Column(nullable = false) private String title;
    @Column(length = 2000) private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true) private List<Objective> objectives = new ArrayList<>();
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PlanStatus status = PlanStatus.DRAFT;
}
