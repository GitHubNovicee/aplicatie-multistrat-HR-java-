package org.hr.infrastructure;

import org.hr.domain.recruitment.Offer;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class OfferEntityRepositoryBase extends InMemoryEntityRepositoryBase<Offer> implements IOfferEntityRepository {
    public OfferEntityRepositoryBase() {
        super("Offer");
    }
}
