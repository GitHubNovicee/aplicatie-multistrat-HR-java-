package org.hr.domain.relations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.Employee;

import java.time.Instant;

/** Entitate componenta a agregatului {@link EmployeeCase} (VR-29): istoric de actiuni. */
@Getter @Setter @NoArgsConstructor
public class CaseAction extends BaseEntity {
    @NotNull(message = "Performer is required!")
    private Employee performedBy;
    @NotBlank(message = "Action description is required!")
    @Size(max = 2000)
    private String description;
    @NotNull(message = "Action time is required!")
    private Instant performedAt;
}
