package org.hr.domain.organization;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/** RQ-03: cont de utilizator per angajat, cu unul sau mai multe roluri si posibilitate de dezactivare. */
@Entity
@Table(name = "user_account", uniqueConstraints = @UniqueConstraint(name = "uk_user_account_username", columnNames = "username"))
@Getter @Setter @NoArgsConstructor
public class UserAccount extends BaseEntity {
    @NotBlank(message = "Username is required!")
    @Size(min = 3, max = 50, message = "Username must have between 3 and 50 characters!")
    @Column(nullable = false)
    private String username;
    @NotBlank(message = "Password hash is required!")
    @Column(nullable = false)
    private String passwordHash;
    @NotNull(message = "Employee is required!")
    @ManyToOne(optional = false)
    private Employee employee;
    @NotEmpty(message = "An account must have at least one role!")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_account_role", joinColumns = @JoinColumn(name = "user_account_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();
    private boolean enabled = true;

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
