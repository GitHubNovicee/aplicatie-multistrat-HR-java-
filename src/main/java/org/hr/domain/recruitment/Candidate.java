package org.hr.domain.recruitment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.common.ContactInfo;

/** RQ-04/RQ-05: candidat (identificat unic prin email). */
@Getter @Setter @NoArgsConstructor
public class Candidate extends BaseEntity {
    @NotBlank(message = "First name is required!")
    @Size(max = 100)
    private String firstName;
    @NotBlank(message = "Last name is required!")
    @Size(max = 100)
    private String lastName;
    @NotNull(message = "Contact info is required!")
    @Valid
    private ContactInfo contactInfo;
    private String cvPath;
}
