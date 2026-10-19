package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class CaseAction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private EmployeeCase employeeCase;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee performedBy;
    @Column(nullable = false, length = 2000) private String description;
    @Column(nullable = false) private Instant performedAt;
}
