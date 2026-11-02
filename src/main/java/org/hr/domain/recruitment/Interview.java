package org.hr.domain.recruitment;

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
@Getter @Setter @NoArgsConstructor
public class Interview extends BaseEntity {
    @NotNull(message = "Application is required!")
    private Application application;
    @NotNull(message = "Interview date/time is required!")
    private LocalDateTime scheduledAt;
    private String location;
    @Min(value = 1, message = "Round number must be at least 1!")
    private int roundNumber = 1;
    @NotEmpty(message = "An interview needs at least one interviewer!")
    private Set<Employee> interviewers = new HashSet<>();
    @NotNull
    private InterviewStatus status = InterviewStatus.SCHEDULED;
    @Valid
    private List<InterviewFeedback> feedbacks = new ArrayList<>();

    public InterviewFeedback addFeedback(InterviewFeedback feedback) {
        return addComponent(feedbacks, feedback);
    }

    public boolean hasFeedbackFrom(Employee interviewer) {
        return feedbacks.stream().anyMatch(f -> f.getInterviewer().equals(interviewer));
    }
}
