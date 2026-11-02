package org.hr.infrastructure;

/** Populeaza repository-urile cu date demo (analog initDomainServiceEntities din modelul msd.t2_c4). */
public interface IHrSampleDataFactory {
    String ADMIN_USERNAME = "admin";
    String RECRUITER_USERNAME = "recruiter";
    String TRAINER_USERNAME = "trainer";
    String EMP_REL_USERNAME = "emprel";
    String MANAGER_USERNAME = "manager";
    String INTERVIEWER_USERNAME = "interviewer";
    String ENGINEER_USERNAME_PREFIX = "engineer";   // engineer1 .. engineer4
    String SAMPLE_COURSE_CODE = "JAVA-101";
    String SAMPLE_COMPETENCY = "Java";

    void initDomainServiceEntities();
}
