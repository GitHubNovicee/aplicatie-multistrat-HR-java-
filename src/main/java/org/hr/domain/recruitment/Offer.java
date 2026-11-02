package org.hr.domain.recruitment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RQ-04: oferta pentru o candidatura (cel mult una - VR-22). */
@Getter @Setter @NoArgsConstructor
public class Offer extends BaseEntity {
    @NotNull(message = "Application is required!")
    private Application application;
    @NotNull(message = "Proposed salary is required!")
    @Positive(message = "Proposed salary must be positive!")
    private BigDecimal proposedSalary;
    private LocalDate startDate;
    @NotNull(message = "Offer validity date is required!")
    private LocalDate validUntil;
    private LocalDate respondedOn;
    @NotNull
    private OfferStatus status = OfferStatus.DRAFT;
}
