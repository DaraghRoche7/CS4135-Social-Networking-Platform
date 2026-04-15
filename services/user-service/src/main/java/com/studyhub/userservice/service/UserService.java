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

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return new UserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getCourse() != null) user.setCourse(request.getCourse());
        if (request.getYear() != null) user.setYear(request.getYear());
        if (request.getModules() != null) user.setModules(request.getModules());

        return new UserProfileResponse(userRepository.save(user));
    }

    @Transactional
    public void follow(String followerEmail, UUID targetUserId) {
        User follower = userRepository.findByEmail(followerEmail).orElseThrow(() -> new UserNotFoundException(followerEmail));
        User following = userRepository.findById(targetUserId).orElseThrow(() -> new UserNotFoundException(targetUserId));
        if (follower.getUserId().equals(following.getUserId())) {
            throw new SelfFollowException();
        }
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new AlreadyFollowingException();
        }
        followRepository.save(new Follow(follower, following));
        eventPublisher.publishUserFollowed(follower.getUserId(), following.getUserId());
    }

    @Transactional
    public void unfollow(String followerEmail, UUID targetUserId) {
        User follower = userRepository.findByEmail(followerEmail).orElseThrow(() -> new UserNotFoundException(followerEmail));
        User following = userRepository.findById(targetUserId).orElseThrow(() -> new UserNotFoundException(targetUserId));
        Follow follow = followRepository.findByFollowerAndFollowing(follower, following).orElseThrow(NotFollowingException::new);
        followRepository.delete(follow);
    }

    public List<UserSummaryResponse> getFollowers(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return followRepository.findByFollowing(user).stream().map(f -> new UserSummaryResponse(f.getFollower())).toList();
    }

    public List<UserSummaryResponse> getFollowing(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return followRepository.findByFollower(user).stream().map(f -> new UserSummaryResponse(f.getFollowing())).toList();
    }
}