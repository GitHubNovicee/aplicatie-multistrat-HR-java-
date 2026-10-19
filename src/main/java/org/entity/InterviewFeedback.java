package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"interview_id", "interviewer_id"}))
public class InterviewFeedback extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Interview interview;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Employee interviewer;
    private int score; // 1-10, validated in service layer
    @Column(length = 2000) private String comments;
    @Enumerated(EnumType.STRING) private Recommendation recommendation;
}
