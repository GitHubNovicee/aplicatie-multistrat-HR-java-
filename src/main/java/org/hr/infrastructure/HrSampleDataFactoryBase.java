package org.hr.infrastructure;

import jakarta.annotation.PostConstruct;
import org.hr.domain.organization.*;
import org.hr.domain.training.Competency;
import org.hr.domain.training.Course;
import org.hr.domain.leave.LeaveType;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;
import java.util.logging.Logger;

@Component
public class HrSampleDataFactoryBase implements IHrSampleDataFactory {
    private static final Logger logger = Logger.getLogger(HrSampleDataFactoryBase.class.getName());

    private final IEmployeeEntityFactory employeeFactory;
    private final ITrainingEntityFactory trainingFactory;
    private final ILeaveEntityFactory leaveFactory;
    private final IDepartmentEntityRepository departmentRepository;
    private final IJobPositionEntityRepository jobPositionRepository;
    private final IEmployeeEntityRepository employeeRepository;
    private final IUserAccountEntityRepository accountRepository;
    private final ICompetencyEntityRepository competencyRepository;
    private final ICourseEntityRepository courseRepository;
    private final ILeaveBalanceEntityRepository balanceRepository;
    private final Clock clock;

    public HrSampleDataFactoryBase(IEmployeeEntityFactory employeeFactory, ITrainingEntityFactory trainingFactory,
                                   ILeaveEntityFactory leaveFactory,
                                   IDepartmentEntityRepository departmentRepository,
                                   IJobPositionEntityRepository jobPositionRepository,
                                   IEmployeeEntityRepository employeeRepository,
                                   IUserAccountEntityRepository accountRepository,
                                   ICompetencyEntityRepository competencyRepository,
                                   ICourseEntityRepository courseRepository,
                                   ILeaveBalanceEntityRepository balanceRepository, Clock clock) {
        this.employeeFactory = employeeFactory;
        this.trainingFactory = trainingFactory;
        this.leaveFactory = leaveFactory;
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.competencyRepository = competencyRepository;
        this.courseRepository = courseRepository;
        this.balanceRepository = balanceRepository;
        this.clock = clock;
    }

    private Employee person(String first, String last, String username, Department d, JobPosition p,
                            Employee manager, Set<Role> roles) {
        Employee e = employeeFactory.buildEmployee(first, last, username + "@hr.example", "0712345678",
                LocalDate.now(clock).minusYears(2), d, p, manager);
        employeeRepository.add(e);
        accountRepository.add(employeeFactory.buildUserAccount(e, username, "{reset-required}", roles));
        return e;
    }

    @PostConstruct
    @Override
    public void initDomainServiceEntities() {
        logger.info(">> PostConstruct :: initDomainServiceEntities");
        Department hr = departmentRepository.add(employeeFactory.buildDepartment("Human Resources", null));
        Department eng = departmentRepository.add(employeeFactory.buildDepartment("Engineering", null));
        JobPosition hrPos = jobPositionRepository.add(employeeFactory.buildJobPosition("HR Specialist", "G3"));
        JobPosition mgrPos = jobPositionRepository.add(employeeFactory.buildJobPosition("Engineering Manager", "G5"));
        JobPosition devPos = jobPositionRepository.add(employeeFactory.buildJobPosition("Software Engineer", "G2"));

        Employee admin = person("Ana", "Admin", ADMIN_USERNAME, hr, hrPos, null, Set.of(Role.HR_ADMIN));
        person("Radu", "Recrutor", RECRUITER_USERNAME, hr, hrPos, admin, Set.of(Role.HR_RECRUITER));
        person("Tudor", "Trainer", TRAINER_USERNAME, hr, hrPos, admin, Set.of(Role.HR_TRAINING_COORDINATOR));
        person("Elena", "Relatii", EMP_REL_USERNAME, hr, hrPos, admin, Set.of(Role.HR_EMPLOYEE_RELATIONS));
        Employee manager = person("Mihai", "Manager", MANAGER_USERNAME, eng, mgrPos, null,
                Set.of(Role.MANAGER, Role.EMPLOYEE));
        person("Ioana", "Interviewer", INTERVIEWER_USERNAME, eng, devPos, manager,
                Set.of(Role.INTERVIEWER, Role.EMPLOYEE));
        for (int i = 1; i <= 4; i++)
            person("Engineer" + i, "Sample", ENGINEER_USERNAME_PREFIX + i, eng, devPos, manager, Set.of(Role.EMPLOYEE));
        employeeRepository.toCollection().stream().filter(e -> manager.equals(e.getManager())
                && e.getEmployeeCode() != null)
                .forEach(e -> balanceRepository.add(leaveFactory.buildLeaveBalance(
                        e, LeaveType.ANNUAL, LocalDate.now(clock).getYear(), 21)));
        hr.setHead(admin);
        departmentRepository.add(hr);

        Competency java = competencyRepository.add(trainingFactory.buildCompetency(SAMPLE_COMPETENCY, "Technical", "Java programming"));
        Course course = trainingFactory.buildCourse(SAMPLE_COURSE_CODE, "Java fundamentals", 24, Set.of(java));
        courseRepository.add(course);
        logger.info(">> EntityRepository employee.count :: " + employeeRepository.size());
    }
}
