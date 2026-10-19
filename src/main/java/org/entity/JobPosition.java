package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class JobPosition extends BaseEntity {
    @Column(nullable = false, unique = true) private String title;
    private String description;
    private String grade;
    private boolean active = true;
}
