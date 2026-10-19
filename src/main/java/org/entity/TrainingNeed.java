package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class TrainingNeed extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Competency competency;
    @Enumerated(EnumType.STRING) private ProficiencyLevel targetLevel;
    @Column(length = 1000) private String justification;
    @ManyToOne(fetch = FetchType.LAZY) private Course suggestedCourse;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TrainingNeedStatus status = TrainingNeedStatus.IDENTIFIED;
}
