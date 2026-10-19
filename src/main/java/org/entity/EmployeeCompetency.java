package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "competency_id"}))
public class EmployeeCompetency extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Competency competency;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ProficiencyLevel level;
    private LocalDate assessedOn;
}
