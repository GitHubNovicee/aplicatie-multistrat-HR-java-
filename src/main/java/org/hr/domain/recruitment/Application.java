package org.hr.domain.recruitment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.time.LocalDate;

/** RQ-05: candidatura unui candidat la un anunt (una singura data - VR-20). */
@Entity
@Table(name = "job_application")
@Getter @Setter @NoArgsConstructor
public class Application extends BaseEntity {
    @NotNull(message = "Candidate is required!")
    @ManyToOne(optional = false)
    private Candidate candidate;
    @NotNull(message = "Job opening is required!")
    @ManyToOne(optional = false)
    private JobOpening jobOpening;
    @NotNull(message = "Application date is required!")
    @Column(nullable = false)
    private LocalDate appliedOn;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.RECEIVED;
    /** computed field (CR-06) */
    @Transient
    private Double averageScore;
}
