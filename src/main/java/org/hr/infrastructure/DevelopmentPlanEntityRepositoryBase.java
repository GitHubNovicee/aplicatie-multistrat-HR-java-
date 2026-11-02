package org.hr.infrastructure;

import org.hr.domain.development.DevelopmentPlan;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class DevelopmentPlanEntityRepositoryBase extends InMemoryEntityRepositoryBase<DevelopmentPlan> implements IDevelopmentPlanEntityRepository {
    public DevelopmentPlanEntityRepositoryBase() {
        super("DevelopmentPlan");
    }
}
