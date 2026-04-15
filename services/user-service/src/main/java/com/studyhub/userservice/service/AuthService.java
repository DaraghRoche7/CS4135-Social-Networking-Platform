package com.studyhub.userservice.service;

import com.studyhub.userservice.dto.AuthResponse;
import com.studyhub.userservice.dto.LoginRequest;
import com.studyhub.userservice.dto.RegisterRequest;
import com.studyhub.userservice.exception.EmailAlreadyExistsException;
import com.studyhub.userservice.exception.InvalidEmailDomainException;
import com.studyhub.userservice.exception.InvalidRefreshTokenException;
import com.studyhub.userservice.exception.UserNotFoundException;
import com.studyhub.userservice.model.Role;
import com.studyhub.userservice.model.RoleName;
import com.studyhub.userservice.model.User;
import com.studyhub.userservice.repository.RoleRepository;
import com.studyhub.userservice.repository.UserRepository;
import com.studyhub.userservice.security.JwtService;
import com.studyhub.userservice.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final Set<String> ALLOWED_DOMAINS = Set.of("ul.ie", "studentmail.ul.ie");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final EventPublisher eventPublisher;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateEmailDomain(request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role not found — ensure DataInitializer ran"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(studentRole);
        user.setRefreshToken(UUID.randomUUID().toString());

        User saved = userRepository.save(user);
        eventPublisher.publishUserRegistered(saved.getUserId(), saved.getEmail(), saved.getName());

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String jwt = jwtService.generateToken(userDetails, Map.of("userId", saved.getUserId().toString()));

        return new AuthResponse(jwt, saved.getRefreshToken(), saved.getUserId(), saved.getName(), saved.getEmail(), saved.getRole().getName().name());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        user.setRefreshToken(UUID.randomUUID().toString());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwt = jwtService.generateToken(userDetails, Map.of("userId", user.getUserId().toString()));

        return new AuthResponse(jwt, user.getRefreshToken(), user.getUserId(), user.getName(), user.getEmail(), user.getRole().getName().name());
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        user.setRefreshToken(UUID.randomUUID().toString());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwt = jwtService.generateToken(userDetails, Map.of("userId", user.getUserId().toString()));

        return new AuthResponse(jwt, user.getRefreshToken(), user.getUserId(), user.getName(), user.getEmail(), user.getRole().getName().name());
    }

    @Transactional
    public void logout(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    private void validateEmailDomain(String email) {
        if (email == null) throw new InvalidEmailDomainException("");
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0) throw new InvalidEmailDomainException(email);
        String domain = email.substring(atIndex + 1).toLowerCase();
        if (!ALLOWED_DOMAINS.contains(domain)) {
            throw new InvalidEmailDomainException(email);
        }
    }
}
