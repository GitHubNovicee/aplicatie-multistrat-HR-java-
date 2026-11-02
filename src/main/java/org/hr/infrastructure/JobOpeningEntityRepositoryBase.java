package org.hr.infrastructure;

import org.hr.domain.recruitment.JobOpening;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class JobOpeningEntityRepositoryBase extends InMemoryEntityRepositoryBase<JobOpening> implements IJobOpeningEntityRepository {
    public JobOpeningEntityRepositoryBase() {
        super("JobOpening");
    }
}
