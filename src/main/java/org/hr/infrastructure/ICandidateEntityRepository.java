package org.hr.infrastructure;

import org.hr.domain.recruitment.Candidate;

import java.util.Optional;

public interface ICandidateEntityRepository extends IEntityRepository<Candidate> {
    Optional<Candidate> findByContactInfoEmail(String email);
}
