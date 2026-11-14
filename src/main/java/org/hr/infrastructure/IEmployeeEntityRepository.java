package org.hr.infrastructure;

import org.hr.domain.organization.Department;
import org.hr.domain.organization.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IEmployeeEntityRepository extends IEntityRepository<Employee> {
    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByContactInfoEmail(String email);

    boolean existsByContactInfoEmail(String email);

    List<Employee> findByManager(Employee manager);

    List<Employee> findByDepartmentAndArchivedFalse(Department department);

    /** Filtru personalizat: angajatii activi (nearhivati) al caror nume contine textul dat. */
    @Query("""
            select e from Employee e
            where e.archived = false
              and (lower(e.firstName) like lower(concat('%', :text, '%'))
                or lower(e.lastName) like lower(concat('%', :text, '%')))
            order by e.lastName, e.firstName
            """)
    List<Employee> searchActiveByName(@Param("text") String text);

    /** VR-28 / RQ-01: angajatii nu se sterg fizic; se arhiveaza logic (Employee.archived). */
    @Override
    default boolean remove(Employee entity) {
        throw new UnsupportedOperationException("Employees are never physically deleted; archive them instead!");
    }

    @Override
    default void delete(Employee entity) {
        throw new UnsupportedOperationException("Employees are never physically deleted; archive them instead!");
    }
}
