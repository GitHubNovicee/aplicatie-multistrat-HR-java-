package org.hr.domain.recruitment;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Radacina agregatului Interview (RQ-05): runda de interviu cu lista intervievatorilor
 * si feedback-urile (entitati componenta) - invariantul VR-21 este local agregatului.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class Interview extends BaseEntity {
    @NotNull(message = "Application is required!")
    @ManyToOne(optional = false)
    private Application application;
    @NotNull(message = "Interview date/time is required!")
    @Column(nullable = false)
    private LocalDateTime scheduledAt;
    private String location;
    @Min(value = 1, message = "Round number must be at least 1!")
    private int roundNumber = 1;
    @NotEmpty(message = "An interview needs at least one interviewer!")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "interview_interviewer", joinColumns = @JoinColumn(name = "interview_id"),
            inverseJoinColumns = @JoinColumn(name = "interviewer_id"))
    private Set<Employee> interviewers = new HashSet<>();
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;
    @Valid
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<InterviewFeedback> feedbacks = new ArrayList<>();

    public InterviewFeedback addFeedback(InterviewFeedback feedback) {
        feedback.setInterview(this);
        return addComponent(feedbacks, feedback);
    }

    public boolean hasFeedbackFrom(Employee interviewer) {
        return feedbacks.stream().anyMatch(f -> f.getInterviewer().equals(interviewer));
    }
}
