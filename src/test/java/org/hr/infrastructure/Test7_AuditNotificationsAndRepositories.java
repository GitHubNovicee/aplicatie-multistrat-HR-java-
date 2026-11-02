package org.hr.infrastructure;

import org.hr.HrTestSupport;
import org.hr.application.IDevelopmentWorkflowService;
import org.hr.domain.common.ConcurrentUpdateException;
import org.hr.domain.common.EntityNotFoundException;
import org.hr.domain.common.HrAuditView;
import org.hr.domain.notification.Notification;
import org.hr.domain.notification.NotificationType;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.services.IHrAuditingService;
import org.hr.services.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** AUDIT + NOTIFICATION Service Tests si teste ale infrastructurii (RQ-11, RQ-12, CR-11, VR-28). */
public class Test7_AuditNotificationsAndRepositories extends HrTestSupport {
    @Autowired private IHrAuditingService auditingService;
    @Autowired private INotificationService notificationService;
    @Autowired private IDevelopmentWorkflowService development;

    @Test
    void workflowOperationsAreAudited() {
        Long deptId = organizationWorkflow.createDepartment(adminId(), "Dept-" + System.nanoTime(), null);
        List<HrAuditView> trail = auditingService.getAuditTrailOf("Department", deptId);
        assertEquals(1, trail.size());
        assertEquals(HrAuditView.AuditOperation.CREATED, trail.get(0).getOperation());
        assertNotNull(trail.get(0).getAt());

        Long emp = newEmployee(Role.EMPLOYEE);
        organizationWorkflow.archiveEmployee(adminId(), emp, null);
        assertTrue(auditingService.getAuditTrailOf("Employee", emp).stream()
                .anyMatch(a -> a.getOperation() == HrAuditView.AuditOperation.ARCHIVED));
    }

    @Test
    void deadlineNotificationsAreGeneratedOnceAndCanBeMarkedRead() {
        Long manager = idOf(IHrSampleDataFactory.MANAGER_USERNAME);
        Long emp = newEmployee(Role.EMPLOYEE);
        Long planId = development.createDevelopmentPlan(manager, emp, "Deadlines", null,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        development.addObjective(manager, planId, "Soon", null, null, LocalDate.now().plusDays(3));
        development.activatePlan(manager, planId);

        assertTrue(notificationService.generateDeadlineNotifications(7) >= 1);
        List<Notification> mine = notificationService.getNotificationsOf(emp).stream()
                .filter(n -> n.getType() == NotificationType.DEADLINE).toList();
        assertEquals(1, mine.size());
        assertEquals(0, notificationService.generateDeadlineNotifications(7));           // fara dubluri

        assertFalse(mine.get(0).isRead());
        assertTrue(notificationService.markAsRead(mine.get(0).getId()).isRead());
    }

    @Test
    void cr11_rq12_auditFieldsAndOptimisticLocking() {
        Long id = newEmployee(Role.EMPLOYEE);
        Employee e = employee(id);
        assertNotNull(e.getCreatedAt());
        long versionBefore = e.getVersion();
        var createdAt = e.getCreatedAt();

        // copie detasata, citita inainte de modificare (versiune veche)
        Employee stale = new Employee();
        stale.setId(e.getId());
        stale.setVersion(e.getVersion());
        stale.setCreatedAt(e.getCreatedAt());

        e.setFirstName("Changed");
        employeeRepository.add(e);
        assertEquals(versionBefore + 1, e.getVersion());                                  // RQ-12: versiune incrementata
        assertEquals(createdAt, e.getCreatedAt());                                        // CR-11: createdAt neschimbat
        assertFalse(e.getUpdatedAt().isBefore(createdAt));                                // CR-11: updatedAt actualizat

        // modificarea concurenta cu versiune veche este respinsa
        assertThrows(ConcurrentUpdateException.class, () -> employeeRepository.add(stale));
    }

    @Test
    void employeesAreNeverPhysicallyDeleted_vr28() {
        Employee e = employee(newEmployee(Role.EMPLOYEE));
        assertThrows(UnsupportedOperationException.class, () -> employeeRepository.remove(e));
        assertNotNull(employeeRepository.getById(e.getId()));
    }

    @Test
    void requireByIdThrowsForUnknownId() {
        assertThrows(EntityNotFoundException.class, () -> employeeRepository.requireById(-1L));
        assertNull(employeeRepository.getById(-1L));
    }
}
