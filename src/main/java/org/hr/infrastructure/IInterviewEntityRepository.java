package org.hr.infrastructure;

import org.hr.domain.recruitment.Application;
import org.hr.domain.recruitment.Interview;
import org.hr.domain.recruitment.InterviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IInterviewEntityRepository extends IEntityRepository<Interview> {
    List<Interview> findByApplication(Application application);

    List<Interview> findByStatusAndScheduledAtBetween(InterviewStatus status, LocalDateTime from, LocalDateTime to);
}
