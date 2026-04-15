package com.studyhub.coreservice.auth.persistence;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyHubUserRepository extends JpaRepository<StudyHubUser, Long> {

    Optional<StudyHubUser> findByEmailIgnoreCase(String email);

    Optional<StudyHubUser> findByPublicId(String publicId);

    boolean existsByEmailIgnoreCase(String email);
}
