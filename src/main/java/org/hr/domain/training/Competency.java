package org.hr.domain.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

/** RQ-07/RQ-08: competenta din catalog. */
@Getter @Setter @NoArgsConstructor
public class Competency extends BaseEntity {
    @NotBlank(message = "Competency name is required!")
    @Size(max = 100)
    private String name;
    @Size(max = 100)
    private String category;
    @Size(max = 1000)
    private String description;
}
