package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "employee")
public class Employee extends BaseEntity {
    @Column(nullable = false, unique = true) private String employeeCode;
    @Column(nullable = false) private String firstName;
    @Column(nullable = false) private String lastName;
    @Column(nullable = false, unique = true) private String email;
    private String phone;
    private LocalDate birthDate;
    @Column(nullable = false) private LocalDate hireDate;
    private LocalDate terminationDate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Department department;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private JobPosition jobPosition;
    @ManyToOne(fetch = FetchType.LAZY) private Employee manager;
    /** logical archiving - never physically deleted */
    private boolean archived = false;
    private Instant archivedAt;
}
