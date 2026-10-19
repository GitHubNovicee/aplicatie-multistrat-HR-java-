package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class RecruitmentRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Department department;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private JobPosition jobPosition;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee requestedBy;
    private int headcount = 1;
    @Column(length = 2000) private String justification;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RequestStatus status = RequestStatus.DRAFT;
}
