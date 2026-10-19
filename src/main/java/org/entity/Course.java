package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class Course extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String title;
    @Column(length = 2000) private String description;
    private int durationHours;
    private boolean active = true;
    @ManyToMany @JoinTable(name = "course_competencies") private Set<Competency> targetCompetencies = new HashSet<>();
}
