package org.hr.infrastructure;

import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.organization.UserAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserAccountEntityRepository extends IEntityRepository<UserAccount> {
    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmployeeAndEnabledTrue(Employee employee);

    /** Filtru personalizat: conturile active care au rolul dat. */
    @Query("select distinct a from UserAccount a join a.roles r where r = :role and a.enabled = true")
    List<UserAccount> findEnabledByRole(@Param("role") Role role);
}
