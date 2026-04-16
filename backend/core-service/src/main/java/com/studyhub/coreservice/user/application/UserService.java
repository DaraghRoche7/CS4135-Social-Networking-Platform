package com.studyhub.coreservice.user.application;

import com.studyhub.coreservice.auth.api.dto.UserSummaryResponse;
import com.studyhub.coreservice.auth.application.UserNotFoundException;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.domain.UserFollow;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import com.studyhub.coreservice.auth.persistence.UserFollowRepository;
import com.studyhub.coreservice.user.api.dto.UpdateUserProfileRequest;
import com.studyhub.coreservice.user.api.dto.UserListResponse;
import com.studyhub.coreservice.user.api.dto.UserProfileResponse;
import java.time.Clock;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final StudyHubUserRepository studyHubUserRepository;
    private final UserFollowRepository userFollowRepository;
    private final Clock clock;

    public UserService(
        StudyHubUserRepository studyHubUserRepository,
        UserFollowRepository userFollowRepository,
        Clock clock
    ) {
        this.studyHubUserRepository = studyHubUserRepository;
        this.userFollowRepository = userFollowRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        return toProfileResponse(getRequiredUser(userId));
    }

    @Transactional(readOnly = true)
    public UserListResponse getFollowers(String userId) {
        StudyHubUser user = getRequiredUser(userId);
        return new UserListResponse(userFollowRepository.findByFollowedOrderByCreatedAtDesc(user).stream()
            .map(UserFollow::getFollower)
            .map(UserSummaryResponse::from)
            .toList());
    }

    @Transactional(readOnly = true)
    public UserListResponse getFollowing(String userId) {
        StudyHubUser user = getRequiredUser(userId);
        return new UserListResponse(userFollowRepository.findByFollowerOrderByCreatedAtDesc(user).stream()
            .map(UserFollow::getFollowed)
            .map(UserSummaryResponse::from)
            .toList());
    }

    @Transactional
    public UserProfileResponse updateProfile(
        String targetUserId,
        UpdateUserProfileRequest request,
        String authenticatedUserId,
        boolean admin
    ) {
        StudyHubUser user = getRequiredUser(targetUserId);
        requireSelfOrAdmin(user, authenticatedUserId, admin);

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.equalsIgnoreCase(user.getEmail())
            && studyHubUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email address is already in use");
        }

        user.updateProfile(normalizedEmail, request.displayName().trim());
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse followUser(String targetUserId, String authenticatedUserId) {
        StudyHubUser follower = getRequiredUser(authenticatedUserId);
        StudyHubUser followed = getRequiredUser(targetUserId);
        if (follower.getPublicId().equals(followed.getPublicId())) {
            throw new IllegalArgumentException("You cannot follow your own account");
        }

        if (!userFollowRepository.existsByFollowerAndFollowed(follower, followed)) {
            userFollowRepository.save(new UserFollow(follower, followed, clock.instant()));
        }
        return toProfileResponse(followed);
    }

    @Transactional
    public UserProfileResponse unfollowUser(String targetUserId, String authenticatedUserId) {
        StudyHubUser follower = getRequiredUser(authenticatedUserId);
        StudyHubUser followed = getRequiredUser(targetUserId);
        userFollowRepository.findByFollowerAndFollowed(follower, followed)
            .ifPresent(userFollowRepository::delete);
        return toProfileResponse(followed);
    }

    private StudyHubUser getRequiredUser(String userId) {
        return studyHubUserRepository.findByPublicId(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserProfileResponse toProfileResponse(StudyHubUser user) {
        return UserProfileResponse.from(
            user,
            userFollowRepository.countByFollowed(user),
            userFollowRepository.countByFollower(user)
        );
    }

    private void requireSelfOrAdmin(StudyHubUser user, String authenticatedUserId, boolean admin) {
        if (!admin && !user.getPublicId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You cannot update another user's profile");
        }
    }
}
