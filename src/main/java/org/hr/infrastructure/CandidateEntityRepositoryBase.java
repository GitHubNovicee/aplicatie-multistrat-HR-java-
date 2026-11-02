package org.hr.infrastructure;

import org.hr.domain.recruitment.Candidate;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class CandidateEntityRepositoryBase extends InMemoryEntityRepositoryBase<Candidate> implements ICandidateEntityRepository {
    public CandidateEntityRepositoryBase() {
        super("Candidate");
    }
}
