package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class LeaveRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LeaveType type;
    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    private int workingDays;
    @Column(length = 1000) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LeaveStatus status = LeaveStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY) private Employee decidedBy;
    private Instant decidedAt;
    @Column(length = 1000) private String decisionNote;
}
