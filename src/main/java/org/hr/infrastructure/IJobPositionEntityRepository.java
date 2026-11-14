package org.hr.infrastructure;

import org.hr.domain.organization.JobPosition;

import java.util.List;
import java.util.Optional;

public interface IJobPositionEntityRepository extends IEntityRepository<JobPosition> {
    Optional<JobPosition> findByTitle(String title);

    List<JobPosition> findByActiveTrue();
}
