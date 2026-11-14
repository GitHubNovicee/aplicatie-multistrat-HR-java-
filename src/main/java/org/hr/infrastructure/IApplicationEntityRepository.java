package org.hr.infrastructure;

import org.hr.domain.recruitment.Application;
import org.hr.domain.recruitment.ApplicationStatus;
import org.hr.domain.recruitment.Candidate;
import org.hr.domain.recruitment.JobOpening;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IApplicationEntityRepository extends IEntityRepository<Application> {
    Optional<Application> findByCandidateAndJobOpening(Candidate candidate, JobOpening jobOpening);

    List<Application> findByJobOpening(JobOpening jobOpening);

    List<Application> findByStatus(ApplicationStatus status);

    /** Filtru personalizat: candidaturile unui anunt aflate inca in procesul de selectie. */
    @Query("select a from Application a where a.jobOpening = :opening and a.status in :statuses order by a.appliedOn")
    List<Application> findOfOpeningWithStatusIn(@Param("opening") JobOpening opening,
                                                @Param("statuses") java.util.Collection<ApplicationStatus> statuses);
}
