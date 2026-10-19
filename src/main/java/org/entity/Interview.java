package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class Interview extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Application application;
    @Column(nullable = false) private LocalDateTime scheduledAt;
    private String location;
    private int roundNumber = 1;
    @ManyToMany @JoinTable(name = "interview_interviewers") private Set<Employee> interviewers = new HashSet<>();
    @Enumerated(EnumType.STRING) @Column(nullable = false) private InterviewStatus status = InterviewStatus.SCHEDULED;
}
