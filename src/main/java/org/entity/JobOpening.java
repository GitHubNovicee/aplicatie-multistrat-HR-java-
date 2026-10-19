package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class JobOpening extends BaseEntity {
    @Column(nullable = false, unique = true) private String reference;
    @Column(nullable = false) private String title;
    @Column(length = 4000) private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private RecruitmentRequest recruitmentRequest;
    private LocalDate publishedOn;
    private LocalDate closingDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private OpeningStatus status = OpeningStatus.DRAFT;
}
