package com.studyhub.userservice.repository;

import com.studyhub.userservice.model.Follow;
import com.studyhub.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    List<Follow> findByFollowing(User following);
    List<Follow> findByFollower(User follower);
}