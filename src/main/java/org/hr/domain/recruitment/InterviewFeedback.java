package org.hr.domain.recruitment;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

/** Entitate componenta a agregatului {@link Interview} (UC-10): scor 1-10 si recomandare. */
@Entity
@Getter @Setter @NoArgsConstructor
public class InterviewFeedback extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id")
    private Interview interview;
    @NotNull(message = "Interviewer is required!")
    @ManyToOne(optional = false)
    private Employee interviewer;
    @Min(value = 1, message = "Score must be at least 1!")
    @Max(value = 10, message = "Score must be at most 10!")
    private int score;
    @Size(max = 2000)
    @Column(length = 2000)
    private String comments;
    @NotNull(message = "Recommendation is required!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recommendation recommendation;
}
