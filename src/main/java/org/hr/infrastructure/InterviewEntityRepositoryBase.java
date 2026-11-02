package org.hr.infrastructure;

import org.hr.domain.recruitment.Interview;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class InterviewEntityRepositoryBase extends InMemoryEntityRepositoryBase<Interview> implements IInterviewEntityRepository {
    public InterviewEntityRepositoryBase() {
        super("Interview");
    }
}
