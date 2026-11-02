package org.hr.domain.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/** RQ-07: curs din catalog, legat de competente. */
@Getter @Setter @NoArgsConstructor
public class Course extends BaseEntity {
    @NotBlank(message = "Course code is required!")
    @Size(max = 30)
    private String code;
    @NotBlank(message = "Course title is required!")
    @Size(max = 200)
    private String title;
    @Size(max = 2000)
    private String description;
    @Positive(message = "Course duration must be positive!")
    private int durationHours;
    private boolean active = true;
    private Set<Competency> targetCompetencies = new HashSet<>();
}
