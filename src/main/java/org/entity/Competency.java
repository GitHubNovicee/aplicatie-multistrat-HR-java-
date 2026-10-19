package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class Competency extends BaseEntity {
    @Column(nullable = false, unique = true) private String name;
    private String category;
    @Column(length = 1000) private String description;
}
