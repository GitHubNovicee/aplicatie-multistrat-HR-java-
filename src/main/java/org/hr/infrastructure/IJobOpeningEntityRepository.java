package org.hr.infrastructure;

import org.hr.domain.recruitment.JobOpening;
import org.hr.domain.recruitment.OpeningStatus;
import org.hr.domain.recruitment.RecruitmentRequest;

import java.util.List;
import java.util.Optional;

public interface IJobOpeningEntityRepository extends IEntityRepository<JobOpening> {
    Optional<JobOpening> findByReference(String reference);

    List<JobOpening> findByStatus(OpeningStatus status);

    List<JobOpening> findByRecruitmentRequest(RecruitmentRequest recruitmentRequest);
}
