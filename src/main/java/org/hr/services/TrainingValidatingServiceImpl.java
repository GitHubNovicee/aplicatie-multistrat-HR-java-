package org.hr.services;

import jakarta.validation.Validator;
import org.hr.domain.organization.Employee;
import org.hr.domain.training.*;
import org.hr.infrastructure.ICompetencyEntityRepository;
import org.hr.infrastructure.ICourseEntityRepository;
import org.hr.infrastructure.IEmployeeCompetencyEntityRepository;
import org.hr.infrastructure.ITrainingNeedEntityRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Service
public class TrainingValidatingServiceImpl extends ValidatingServiceSupport implements ITrainingValidatingService {
    private final ICourseEntityRepository courseRepository;
    private final ICompetencyEntityRepository competencyRepository;
    private final IEmployeeCompetencyEntityRepository employeeCompetencyRepository;
    private final ITrainingNeedEntityRepository needRepository;
    private final ITrainingComputingService computingService;
    private final IEmployeeValidatingService employeeValidatingService;
    private final Clock clock;

    public TrainingValidatingServiceImpl(Validator validator,
                                         ICourseEntityRepository courseRepository,
                                         ICompetencyEntityRepository competencyRepository,
                                         IEmployeeCompetencyEntityRepository employeeCompetencyRepository,
                                         ITrainingNeedEntityRepository needRepository,
                                         ITrainingComputingService computingService,
                                         IEmployeeValidatingService employeeValidatingService,
                                         Clock clock) {
        super(validator);
        this.courseRepository = courseRepository;
        this.competencyRepository = competencyRepository;
        this.employeeCompetencyRepository = employeeCompetencyRepository;
        this.needRepository = needRepository;
        this.computingService = computingService;
        this.employeeValidatingService = employeeValidatingService;
        this.clock = clock;
    }

    @Override
    public void validateCourse(Course course) {
        Set<String> violations = new TreeSet<>(beanViolations(course));
        if (course.getCode() != null && courseRepository.findFirst(c ->
                c.getCode().equalsIgnoreCase(course.getCode()) && !c.equals(course)).isPresent())
            violations.add("code: Course code '" + course.getCode() + "' is already used!");
        throwIfAny(violations);
    }

    @Override
    public void validateCompetency(Competency competency) {
        Set<String> violations = new TreeSet<>(beanViolations(competency));
        if (competency.getName() != null && competencyRepository.findFirst(c ->
                c.getName().equalsIgnoreCase(competency.getName()) && !c.equals(competency)).isPresent())
            violations.add("name: Competency '" + competency.getName() + "' already exists!");
        throwIfAny(violations);
    }

    @Override
    public void validateSession(TrainingSession session) {
        Set<String> violations = new TreeSet<>(beanViolations(session));
        if (session.getCourse() != null && !session.getCourse().isActive())
            violations.add("course: Sessions can be planned only for active courses!");
        // data de start se verifica doar pentru sesiuni inca neincepute
        if (session.getStatus() == SessionStatus.PLANNED && session.getEnrollments().isEmpty()
                && session.getStartsAt() != null && !session.getStartsAt().isAfter(LocalDateTime.now(clock)))
            violations.add("startsAt: A new session must start in the future!");
        throwIfAny(violations);
    }

    @Override
    public void validateEnrollment(TrainingSession session, Employee employee) {
        Set<String> violations = new TreeSet<>();
        if (employee.isArchived())                                                          // VR-28
            violations.add("VR-28: Archived employee " + employee.getEmployeeCode() + " cannot be enrolled!");
        if (session.getStatus() != SessionStatus.PLANNED)                                   // VR-23
            violations.add("VR-23: Enrollment is allowed only for PLANNED sessions!");
        if (session.findEnrollmentOf(employee).isPresent())                                 // VR-23
            violations.add("VR-23: An employee can enroll only once in a session!");
        if (computingService.computeAvailableSeats(session) <= 0)                           // VR-23 + CR-05
            violations.add("VR-23: The session has no available seats!");
        throwIfAny(violations);
    }

    @Override
    public void validateEmployeeCompetency(EmployeeCompetency ec) {
        Set<String> violations = new TreeSet<>(beanViolations(ec));
        if (ec.getEmployee() != null && ec.getCompetency() != null
                && employeeCompetencyRepository.findFirst(x -> x.getEmployee().equals(ec.getEmployee())
                && x.getCompetency().equals(ec.getCompetency()) && !x.equals(ec)).isPresent())
            violations.add("VR-24: An employee has only one level per competency!");
        throwIfAny(violations);
    }

    @Override
    public void validateTrainingNeed(TrainingNeed need) {
        Set<String> violations = new TreeSet<>(beanViolations(need));
        if (need.getEmployee() != null) {
            if (need.getEmployee().isArchived())
                violations.add("VR-28: Archived employee " + need.getEmployee().getEmployeeCode()
                        + " cannot receive new training needs!");
            if (need.getCompetency() != null && needRepository.findFirst(n ->
                    n.getEmployee().equals(need.getEmployee()) && n.getCompetency().equals(need.getCompetency())
                            && !n.equals(need)
                            && (n.getStatus() == TrainingNeedStatus.IDENTIFIED || n.getStatus() == TrainingNeedStatus.PLANNED))
                    .isPresent())
                violations.add("competency: An open training need already exists for this competency!");
        }
        throwIfAny(violations);
    }
}
