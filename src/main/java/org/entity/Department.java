package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class Department extends BaseEntity {
    @Column(nullable = false, unique = true) private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY) private Department parent;
    @ManyToOne(fetch = FetchType.LAZY) private Employee head;
    private boolean active = true;
}
