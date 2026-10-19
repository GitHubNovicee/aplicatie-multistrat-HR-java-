package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "type", "year"}))
public class LeaveBalance extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private LeaveType type;
    @Column(name = "year", nullable = false) private int year;
    private int entitledDays;
    private int usedDays;
    public int remainingDays() { return entitledDays - usedDays; }
}
