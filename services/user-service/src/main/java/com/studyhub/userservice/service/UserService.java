package com.studyhub.userservice.service;

import com.studyhub.userservice.dto.UpdateProfileRequest;
import com.studyhub.userservice.dto.UserProfileResponse;
import com.studyhub.userservice.dto.UserSummaryResponse;
import com.studyhub.userservice.exception.AlreadyFollowingException;
import com.studyhub.userservice.exception.NotFollowingException;
import com.studyhub.userservice.exception.SelfFollowException;
import com.studyhub.userservice.exception.UserNotFoundException;
import com.studyhub.userservice.model.Follow;
import com.studyhub.userservice.model.User;
import com.studyhub.userservice.repository.FollowRepository;
import com.studyhub.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final EventPublisher eventPublisher;

    public UserService(UserRepository userRepository, FollowRepository followRepository, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.eventPublisher = eventPublisher;
    }

    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return UserProfileResponse.from(user,
            followRepository.countByFollowing(user),
            followRepository.countByFollower(user));
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
        if (req.getName() != null) user.setName(req.getName());
        if (req.getCourse() != null) user.setCourse(req.getCourse());
        if (req.getYear() != null) user.setYear(req.getYear());
        if (req.getModules() != null) user.setModules(req.getModules());
        userRepository.save(user);
        return UserProfileResponse.from(user,
            followRepository.countByFollowing(user),
            followRepository.countByFollower(user));
    }

    @Transactional
    public void follow(String followerEmail, UUID followingId) {
        User follower = userRepository.findByEmail(followerEmail)
            .orElseThrow(() -> new UserNotFoundException(followerEmail));
        User following = userRepository.findById(followingId)
            .orElseThrow(() -> new UserNotFoundException(followingId.toString()));

        if (follower.getUserId().equals(followingId)) throw new SelfFollowException();
        if (followRepository.existsByFollowerAndFollowing(follower, following)) throw new AlreadyFollowingException();

        Follow f = new Follow();
        f.setFollower(follower);
        f.setFollowing(following);
        followRepository.save(f);
        eventPublisher.publishUserFollowed(follower.getUserId(), followingId);
    }

    @Transactional
    public void unfollow(String followerEmail, UUID followingId) {
        User follower = userRepository.findByEmail(followerEmail)
            .orElseThrow(() -> new UserNotFoundException(followerEmail));
        User following = userRepository.findById(followingId)
            .orElseThrow(() -> new UserNotFoundException(followingId.toString()));

        Follow f = followRepository.findByFollowerAndFollowing(follower, following)
            .orElseThrow(NotFollowingException::new);
        followRepository.delete(f);
    }

    public List<UserSummaryResponse> getFollowers(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return followRepository.findByFollowing(user).stream()
            .map(f -> UserSummaryResponse.from(f.getFollower()))
            .toList();
    }

    public List<UserSummaryResponse> getFollowing(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return followRepository.findByFollower(user).stream()
            .map(f -> UserSummaryResponse.from(f.getFollowing()))
            .toList();
    }

    public List<UserSummaryResponse> search(String name) {
        return userRepository.findByNameContainingIgnoreCase(name).stream()
            .map(UserSummaryResponse::from)
            .toList();
    }
}