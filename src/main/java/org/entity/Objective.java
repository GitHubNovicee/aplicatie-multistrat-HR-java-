package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class Objective extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY) private DevelopmentPlan plan;
    @Column(nullable = false) private String title;
    @Column(length = 2000) private String description;
    @Column(length = 1000) private String expectedResult;
    private String actualResult;
    @Column(nullable = false) private LocalDate dueDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ObjectiveStatus status = ObjectiveStatus.NOT_STARTED;
}
