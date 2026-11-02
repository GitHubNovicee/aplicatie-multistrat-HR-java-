package org.hr.domain.recruitment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.time.LocalDate;

/** RQ-05: candidatura unui candidat la un anunt (una singura data - VR-20). */
@Getter @Setter @NoArgsConstructor
public class Application extends BaseEntity {
    @NotNull(message = "Candidate is required!")
    private Candidate candidate;
    @NotNull(message = "Job opening is required!")
    private JobOpening jobOpening;
    @NotNull(message = "Application date is required!")
    private LocalDate appliedOn;
    @NotNull
    private ApplicationStatus status = ApplicationStatus.RECEIVED;
    /** computed field (CR-06) */
    private Double averageScore;
}
