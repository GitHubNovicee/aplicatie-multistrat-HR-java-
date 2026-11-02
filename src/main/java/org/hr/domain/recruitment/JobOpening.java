package org.hr.domain.recruitment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.time.LocalDate;

/** RQ-04: anunt de angajare. */
@Getter @Setter @NoArgsConstructor
public class JobOpening extends BaseEntity {
    @NotBlank(message = "Opening reference is required!")
    private String reference;
    @NotBlank(message = "Opening title is required!")
    @Size(max = 200)
    private String title;
    @Size(max = 4000, message = "Description is too long!")
    private String description;
    @NotNull(message = "Recruitment request is required!")
    private RecruitmentRequest recruitmentRequest;
    private LocalDate publishedOn;
    private LocalDate closingDate;
    @NotNull
    private OpeningStatus status = OpeningStatus.DRAFT;
}
