package com.studyhub.userservice.service;

import com.studyhub.userservice.dto.AuthResponse;
import com.studyhub.userservice.dto.LoginRequest;
import com.studyhub.userservice.dto.RegisterRequest;
import com.studyhub.userservice.exception.EmailAlreadyExistsException;
import com.studyhub.userservice.model.Role;
import com.studyhub.userservice.model.RoleName;
import com.studyhub.userservice.model.User;
import com.studyhub.userservice.repository.RoleRepository;
import com.studyhub.userservice.repository.UserRepository;
import com.studyhub.userservice.security.JwtService;
import com.studyhub.userservice.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = new Role(RoleName.STUDENT);
    }

    @Test
    void register_withValidULEmail_returnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice Student");
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("password123");

        User savedUser = new User();
        savedUser.setName("Alice Student");
        savedUser.setEmail("alice@studentmail.ul.ie");
        savedUser.setPasswordHash("hashed");
        savedUser.setRole(studentRole);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("alice@studentmail.ul.ie")
                .password("hashed")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        when(userRepository.existsByEmail("alice@studentmail.ul.ie")).thenReturn(false);
        when(roleRepository.findByName(RoleName.STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("alice@studentmail.ul.ie")).thenReturn(userDetails);
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("alice@studentmail.ul.ie");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        verify(eventPublisher).publishUserRegistered(any(), eq("alice@studentmail.ul.ie"), eq("Alice Student"));
    }

    @Test
    void register_withDuplicateEmail_throwsEmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Bob");
        request.setEmail("bob@studentmail.ul.ie");
        request.setPassword("password123");

        when(userRepository.existsByEmail("bob@studentmail.ul.ie")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("bob@studentmail.ul.ie");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("password123");

        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@studentmail.ul.ie");
        user.setPasswordHash("hashed");
        user.setRole(studentRole);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("alice@studentmail.ul.ie")
                .password("hashed")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        when(userRepository.findByEmail("alice@studentmail.ul.ie")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("alice@studentmail.ul.ie")).thenReturn(userDetails);
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_withInvalidCredentials_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@studentmail.ul.ie");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}