package org.hr.domain.recruitment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.time.LocalDate;

/** RQ-04: anunt de angajare. */
@Entity
@Getter @Setter @NoArgsConstructor
public class JobOpening extends BaseEntity {
    @NotBlank(message = "Opening reference is required!")
    @Column(name = "opening_reference", nullable = false)
    private String reference;
    @NotBlank(message = "Opening title is required!")
    @Size(max = 200)
    @Column(nullable = false)
    private String title;
    @Size(max = 4000, message = "Description is too long!")
    @Column(length = 4000)
    private String description;
    @NotNull(message = "Recruitment request is required!")
    @ManyToOne(optional = false)
    private RecruitmentRequest recruitmentRequest;
    private LocalDate publishedOn;
    private LocalDate closingDate;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpeningStatus status = OpeningStatus.DRAFT;
}
