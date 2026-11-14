package org.hr.domain.recruitment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RQ-04: oferta pentru o candidatura (cel mult una - VR-22). */
@Entity
@Getter @Setter @NoArgsConstructor
public class Offer extends BaseEntity {
    @NotNull(message = "Application is required!")
    @ManyToOne(optional = false)
    private Application application;
    @NotNull(message = "Proposed salary is required!")
    @Positive(message = "Proposed salary must be positive!")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal proposedSalary;
    private LocalDate startDate;
    @NotNull(message = "Offer validity date is required!")
    @Column(nullable = false)
    private LocalDate validUntil;
    private LocalDate respondedOn;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.DRAFT;
}
