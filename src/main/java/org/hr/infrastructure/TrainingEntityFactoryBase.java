package org.hr.infrastructure;

import org.hr.domain.organization.Employee;
import org.hr.domain.training.*;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class TrainingEntityFactoryBase implements ITrainingEntityFactory {
    private final ICompetencyEntityRepository competencyRepository;
    private final ICourseEntityRepository courseRepository;
    private final ITrainingSessionEntityRepository sessionRepository;
    private final IEmployeeCompetencyEntityRepository employeeCompetencyRepository;
    private final ITrainingNeedEntityRepository needRepository;
    private final Clock clock;

    public TrainingEntityFactoryBase(ICompetencyEntityRepository competencyRepository,
                                     ICourseEntityRepository courseRepository,
                                     ITrainingSessionEntityRepository sessionRepository,
                                     IEmployeeCompetencyEntityRepository employeeCompetencyRepository,
                                     ITrainingNeedEntityRepository needRepository,
                                     Clock clock) {
        this.competencyRepository = competencyRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
        this.employeeCompetencyRepository = employeeCompetencyRepository;
        this.needRepository = needRepository;
        this.clock = clock;
    }

    @Override
    public Competency buildCompetency(String name, String category, String description) {
        Competency competency = new Competency();
        competency.setId(competencyRepository.getNextID());
        competency.setName(name);
        competency.setCategory(category);
        competency.setDescription(description);
        return competency;
    }

    @Override
    public Course buildCourse(String code, String title, int durationHours, Set<Competency> targetCompetencies) {
        Course course = new Course();
        course.setId(courseRepository.getNextID());
        course.setCode(code);
        course.setTitle(title);
        course.setDurationHours(durationHours);
        course.setActive(true);
        course.setTargetCompetencies(targetCompetencies == null ? new HashSet<>() : new HashSet<>(targetCompetencies));
        return course;
    }

    @Override
    public TrainingSession buildTrainingSession(Course course, LocalDateTime startsAt, LocalDateTime endsAt,
                                                String location, String trainer, int capacity, Employee coordinator) {
        TrainingSession session = new TrainingSession();
        session.setId(sessionRepository.getNextID());
        session.setCourse(course);
        session.setStartsAt(startsAt);
        session.setEndsAt(endsAt);
        session.setLocation(location);
        session.setTrainer(trainer);
        session.setCapacity(capacity);
        session.setCoordinator(coordinator);
        session.setStatus(SessionStatus.PLANNED);
        session.setAvailableSeats(capacity);
        return session;
    }

    @Override
    public Enrollment buildEnrollment(Employee employee) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEmployee(employee);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        return enrollment;
    }

    @Override
    public EmployeeCompetency buildEmployeeCompetency(Employee employee, Competency competency, ProficiencyLevel level) {
        EmployeeCompetency ec = new EmployeeCompetency();
        ec.setId(employeeCompetencyRepository.getNextID());
        ec.setEmployee(employee);
        ec.setCompetency(competency);
        ec.setLevel(level);
        ec.setAssessedOn(LocalDate.now(clock));
        return ec;
    }

    @Override
    public TrainingNeed buildTrainingNeed(Employee employee, Competency competency, ProficiencyLevel targetLevel,
                                          String justification, Course suggestedCourse) {
        TrainingNeed need = new TrainingNeed();
        need.setId(needRepository.getNextID());
        need.setEmployee(employee);
        need.setCompetency(competency);
        need.setTargetLevel(targetLevel);
        need.setJustification(justification);
        need.setSuggestedCourse(suggestedCourse);
        need.setStatus(TrainingNeedStatus.IDENTIFIED);
        return need;
    }
}
