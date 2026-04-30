package com.studyhub.coreservice.module.persistence;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.module.domain.FollowedModule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowedModuleRepository extends JpaRepository<FollowedModule, Long> {

    List<FollowedModule> findByUserOrderByCreatedAtDesc(StudyHubUser user);

    Optional<FollowedModule> findByUserAndModuleCodeIgnoreCase(StudyHubUser user, String moduleCode);

    boolean existsByUserAndModuleCodeIgnoreCase(StudyHubUser user, String moduleCode);
}