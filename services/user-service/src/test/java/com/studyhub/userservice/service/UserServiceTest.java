package com.studyhub.userservice.service;

import com.studyhub.userservice.exception.AlreadyFollowingException;
import com.studyhub.userservice.exception.SelfFollowException;
import com.studyhub.userservice.exception.UserNotFoundException;
import com.studyhub.userservice.model.Follow;
import com.studyhub.userservice.model.Role;
import com.studyhub.userservice.model.RoleName;
import com.studyhub.userservice.model.User;
import com.studyhub.userservice.repository.FollowRepository;
import com.studyhub.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FollowRepository followRepository;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        Role role = new Role(RoleName.STUDENT);
        follower = new User();
        follower.setEmail("follower@studentmail.ul.ie");
        follower.setName("Follower");
        follower.setRole(role);
        following = new User();
        following.setEmail("following@studentmail.ul.ie");
        following.setName("Following");
        following.setRole(role);
    }

    @Test
    void follow_withValidUsers_savesFollowAndPublishesEvent() {
        when(userRepository.findByEmail(follower.getEmail())).thenReturn(Optional.of(follower));
        when(userRepository.findById(following.getUserId())).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenAnswer(i -> i.getArgument(0));
        userService.follow(follower.getEmail(), following.getUserId());
        verify(followRepository).save(any(Follow.class));
        verify(eventPublisher).publishUserFollowed(follower.getUserId(), following.getUserId());
    }

    @Test
    void follow_selfFollow_throwsSelfFollowException() {
        when(userRepository.findByEmail(follower.getEmail())).thenReturn(Optional.of(follower));
        when(userRepository.findById(follower.getUserId())).thenReturn(Optional.of(follower));
        assertThatThrownBy(() -> userService.follow(follower.getEmail(), follower.getUserId())).isInstanceOf(SelfFollowException.class);
        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_alreadyFollowing_throwsAlreadyFollowingException() {
        when(userRepository.findByEmail(follower.getEmail())).thenReturn(Optional.of(follower));
        when(userRepository.findById(following.getUserId())).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        assertThatThrownBy(() -> userService.follow(follower.getEmail(), following.getUserId())).isInstanceOf(AlreadyFollowingException.class);
        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_targetUserNotFound_throwsUserNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findByEmail(follower.getEmail())).thenReturn(Optional.of(follower));
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.follow(follower.getEmail(), unknownId)).isInstanceOf(UserNotFoundException.class);
    }
}