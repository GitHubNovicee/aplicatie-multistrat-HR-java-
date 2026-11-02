package org.hr.domain.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

/** RQ-02: functii cu grad. */
@Getter @Setter @NoArgsConstructor
public class JobPosition extends BaseEntity {
    @NotBlank(message = "Job position title is required!")
    @Size(max = 100, message = "Job position title is too long!")
    private String title;
    @Size(max = 500)
    private String description;
    @Size(max = 30)
    private String grade;
    private boolean active = true;

    public JobPosition(String title, String grade) {
        this.title = title;
        this.grade = grade;
    }
}
