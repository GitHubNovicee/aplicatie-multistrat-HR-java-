package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "employee_case")
public class EmployeeCase extends BaseEntity {
    @Column(nullable = false, unique = true) private String caseNumber; // generated, e.g. ER-2026-0001
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CaseType type;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee submittedBy;
    @ManyToOne(fetch = FetchType.LAZY) private Employee assignedTo; // HR responsible
    @Column(nullable = false) private String subject;
    @Column(length = 4000) private String details;
    @OneToMany(mappedBy = "employeeCase", cascade = CascadeType.ALL, orphanRemoval = true) private List<CaseAction> actions = new ArrayList<>();
    @Column(length = 2000) private String resolution;
    private Instant resolvedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CaseStatus status = CaseStatus.OPEN;
}
