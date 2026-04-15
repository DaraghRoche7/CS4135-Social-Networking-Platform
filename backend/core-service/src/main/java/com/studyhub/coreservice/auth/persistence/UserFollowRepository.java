package com.studyhub.coreservice.auth.persistence;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.domain.UserFollow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    boolean existsByFollowerAndFollowed(StudyHubUser follower, StudyHubUser followed);

    Optional<UserFollow> findByFollowerAndFollowed(StudyHubUser follower, StudyHubUser followed);

    long countByFollowed(StudyHubUser followed);

    long countByFollower(StudyHubUser follower);

    List<UserFollow> findByFollowerOrderByCreatedAtDesc(StudyHubUser follower);

    List<UserFollow> findByFollowedOrderByCreatedAtDesc(StudyHubUser followed);
}
