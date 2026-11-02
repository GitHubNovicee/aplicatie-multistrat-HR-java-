package org.hr.infrastructure;

import org.hr.domain.recruitment.RecruitmentRequest;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class RecruitmentRequestEntityRepositoryBase extends InMemoryEntityRepositoryBase<RecruitmentRequest> implements IRecruitmentRequestEntityRepository {
    public RecruitmentRequestEntityRepositoryBase() {
        super("RecruitmentRequest");
    }
}
