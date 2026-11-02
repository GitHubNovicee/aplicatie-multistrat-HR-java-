package org.hr.domain.recruitment;

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
@Getter @Setter @NoArgsConstructor
public class InterviewFeedback extends BaseEntity {
    @NotNull(message = "Interviewer is required!")
    private Employee interviewer;
    @Min(value = 1, message = "Score must be at least 1!")
    @Max(value = 10, message = "Score must be at most 10!")
    private int score;
    @Size(max = 2000)
    private String comments;
    @NotNull(message = "Recommendation is required!")
    private Recommendation recommendation;
}
