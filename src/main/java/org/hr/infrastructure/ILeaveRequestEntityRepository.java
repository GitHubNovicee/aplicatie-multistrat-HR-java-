package org.hr.infrastructure;

import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveStatus;
import org.hr.domain.organization.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ILeaveRequestEntityRepository extends IEntityRepository<LeaveRequest> {
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    /** Filtru personalizat (VR-25): cererile unui angajat, in anumite stari, care se suprapun cu intervalul dat. */
    @Query("""
            select r from LeaveRequest r
            where r.employee = :employee and r.status in :statuses
              and r.period.startDate <= :end and r.period.endDate >= :start
            order by r.period.startDate
            """)
    List<LeaveRequest> findOverlapping(@Param("employee") Employee employee,
                                       @Param("statuses") Collection<LeaveStatus> statuses,
                                       @Param("start") LocalDate start, @Param("end") LocalDate end);
}
