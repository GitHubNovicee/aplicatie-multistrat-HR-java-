package org.hr.infrastructure;

import org.hr.domain.organization.JobPosition;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class JobPositionEntityRepositoryBase extends InMemoryEntityRepositoryBase<JobPosition> implements IJobPositionEntityRepository {
    public JobPositionEntityRepositoryBase() {
        super("JobPosition");
    }
}
