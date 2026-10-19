package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class Offer extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false) private Application application;
    @Column(precision = 12, scale = 2) private BigDecimal proposedSalary;
    private LocalDate startDate;
    @Column(nullable = false) private LocalDate validUntil;
    private LocalDate respondedOn;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private OfferStatus status = OfferStatus.DRAFT;
}
