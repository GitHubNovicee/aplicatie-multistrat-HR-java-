package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "user_account")
public class UserAccount extends BaseEntity {
    @Column(nullable = false, unique = true) private String username;
    @Column(nullable = false) private String passwordHash;
    @OneToOne(fetch = FetchType.LAZY, optional = false) private Employee employee;
    @ElementCollection(fetch = FetchType.EAGER) @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_account_roles") private Set<Role> roles = new HashSet<>();
    private boolean enabled = true;
}
