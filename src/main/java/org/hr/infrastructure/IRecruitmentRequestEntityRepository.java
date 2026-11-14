package org.hr.infrastructure;

import org.hr.domain.organization.Employee;
import org.hr.domain.recruitment.RecruitmentRequest;
import org.hr.domain.recruitment.RequestStatus;

import java.util.List;

public interface IRecruitmentRequestEntityRepository extends IEntityRepository<RecruitmentRequest> {
    List<RecruitmentRequest> findByStatus(RequestStatus status);

    List<RecruitmentRequest> findByRequestedBy(Employee requestedBy);
}
