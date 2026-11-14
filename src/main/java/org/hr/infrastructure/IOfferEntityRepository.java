package org.hr.infrastructure;

import org.hr.domain.recruitment.Application;
import org.hr.domain.recruitment.Offer;
import org.hr.domain.recruitment.OfferStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IOfferEntityRepository extends IEntityRepository<Offer> {
    Optional<Offer> findByApplication(Application application);

    List<Offer> findByStatusAndValidUntilBetween(OfferStatus status, LocalDate from, LocalDate to);
}
