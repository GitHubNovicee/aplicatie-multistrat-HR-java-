package org.hr;

import org.hr.application.IOrganizationWorkflowService;
import org.hr.domain.organization.Department;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.JobPosition;
import org.hr.domain.organization.Role;
import org.hr.infrastructure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Baza comuna a testelor de integrare: contextul Spring (cu datele demo din HrSampleDataFactoryBase)
 * si metode ajutatoare. Fiecare test isi creeaza proprii angajati (email/cod unic) ca sa fie independent de ordine.
 */
@SpringBootTest
public abstract class HrTestSupport {
    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired protected IEmployeeEntityRepository employeeRepository;
    @Autowired protected IUserAccountEntityRepository accountRepository;
    @Autowired protected IDepartmentEntityRepository departmentRepository;
    @Autowired protected IJobPositionEntityRepository jobPositionRepository;
    @Autowired protected ICompetencyEntityRepository competencyRepository;
    @Autowired protected IOrganizationWorkflowService organizationWorkflow;

    protected Long idOf(String username) {
        return accountRepository.findFirst(a -> a.getUsername().equals(username)).orElseThrow().getEmployee().getId();
    }

    protected Employee employee(Long id) {
        return employeeRepository.requireById(id);
    }

    protected Department engineering() {
        return departmentRepository.findFirst(d -> d.getName().equals("Engineering")).orElseThrow();
    }

    protected JobPosition softwareEngineer() {
        return jobPositionRepository.findFirst(p -> p.getTitle().equals("Software Engineer")).orElseThrow();
    }

    protected Long adminId() {
        return idOf(IHrSampleDataFactory.ADMIN_USERNAME);
    }

    /** Angajat nou in Engineering, cu managerul demo si cont cu rolurile date. */
    protected Long newEmployee(Role... roles) {
        int n = SEQ.incrementAndGet();
        String unique = "t" + n + "x" + System.nanoTime() % 100000;
        Long id = organizationWorkflow.createEmployee(adminId(), "Test" + n, "Person", unique + "@test.example",
                "0722222222", LocalDate.now().minusMonths(6), engineering().getId(), softwareEngineer().getId(),
                idOf(IHrSampleDataFactory.MANAGER_USERNAME));
        organizationWorkflow.createUserAccount(adminId(), id, unique, "{hash}", Set.of(roles));
        return id;
    }

    /** Prima zi de luni (peste &gt;= 10 zile) a carei saptamana lucratoare (L-V) este in acelasi an calendaristic. */
    protected LocalDate nextWorkWeekStart() {
        LocalDate d = LocalDate.now().plusDays(10);
        while (d.getDayOfWeek() != DayOfWeek.MONDAY || d.getYear() != d.plusDays(4).getYear())
            d = d.plusDays(1);
        return d;
    }
}
