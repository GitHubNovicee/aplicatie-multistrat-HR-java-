package org.hr.domain.training;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Radacina agregatului TrainingSession (RQ-07): sesiune cu capacitate si inscrieri
 * (entitati componenta). Invariantii VR-23 / CR-05 sunt locali agregatului.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class TrainingSession extends BaseEntity {
    @NotNull(message = "Course is required!")
    @ManyToOne(optional = false)
    private Course course;
    @NotNull(message = "Start date/time is required!")
    @Column(nullable = false)
    private LocalDateTime startsAt;
    @NotNull(message = "End date/time is required!")
    @Column(nullable = false)
    private LocalDateTime endsAt;
    private String location;
    private String trainer;
    @Min(value = 1, message = "Capacity must be at least 1!")
    private int capacity;
    @ManyToOne(optional = true)
    private Employee coordinator;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.PLANNED;
    @Valid
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Enrollment> enrollments = new ArrayList<>();
    /** computed field (CR-05) */
    @Transient
    private Integer availableSeats;

    public Enrollment addEnrollment(Enrollment enrollment) {
        enrollment.setSession(this);
        return addComponent(enrollments, enrollment);
    }

    public Optional<Enrollment> findEnrollmentOf(Employee employee) {
        return enrollments.stream().filter(e -> e.getEmployee().equals(employee)).findFirst();
    }

    @AssertTrue(message = "Session end must be after its start!")
    public boolean isScheduleValid() {
        return startsAt == null || endsAt == null || endsAt.isAfter(startsAt);
    }
}
