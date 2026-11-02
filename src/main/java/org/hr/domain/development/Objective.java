package org.hr.domain.development;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.time.LocalDate;

/** Entitate componenta a agregatului {@link DevelopmentPlan} (VR-29: nu exista fara planul parinte). */
@Getter @Setter @NoArgsConstructor
public class Objective extends BaseEntity {
    @NotBlank(message = "Objective title is required!")
    @Size(max = 200)
    private String title;
    @Size(max = 2000)
    private String description;
    @Size(max = 1000)
    private String expectedResult;
    private String actualResult;
    @NotNull(message = "Objective due date is required!")
    private LocalDate dueDate;
    @NotNull
    private ObjectiveStatus status = ObjectiveStatus.NOT_STARTED;
}
