package org.hr.infrastructure;

import org.hr.domain.organization.Department;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.JobPosition;
import org.hr.domain.recruitment.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/** Factory pentru agregatele din modulul Recrutare (RQ-04, RQ-05). */
public interface IRecruitmentEntityFactory {
    RecruitmentRequest buildRecruitmentRequest(Employee requestedBy, Department department,
                                               JobPosition jobPosition, int headcount, String justification);

    /** Anunt DRAFT cu referinta generata (JOB-&lt;an&gt;-0001). */
    JobOpening buildJobOpening(RecruitmentRequest request, String title, String description, LocalDate closingDate);

    Candidate buildCandidate(String firstName, String lastName, String email, String phone, String cvPath);

    /** Candidatura RECEIVED, appliedOn = azi. */
    Application buildApplication(Candidate candidate, JobOpening opening);

    Interview buildInterview(Application application, LocalDateTime scheduledAt, String location,
                             Set<Employee> interviewers, int roundNumber);

    /** Componenta fara id; id-ul local este atribuit de Interview.addFeedback. */
    InterviewFeedback buildFeedback(Employee interviewer, int score, String comments, Recommendation recommendation);

    Offer buildOffer(Application application, BigDecimal proposedSalary, LocalDate startDate, LocalDate validUntil);
}
