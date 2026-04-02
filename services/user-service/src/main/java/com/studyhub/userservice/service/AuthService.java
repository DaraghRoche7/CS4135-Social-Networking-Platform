package com.studyhub.userservice.service;

import com.studyhub.userservice.dto.AuthResponse;
import com.studyhub.userservice.dto.LoginRequest;
import com.studyhub.userservice.dto.RegisterRequest;
import com.studyhub.userservice.exception.EmailAlreadyExistsException;
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

@Service
public class AuthService {

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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT).orElseThrow(() -> new IllegalStateException("STUDENT role not found — ensure DataInitializer ran"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(studentRole);
        User saved = userRepository.save(user);
        eventPublisher.publishUserRegistered(saved.getUserId(), saved.getEmail(), saved.getName());

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String token = jwtService.generateToken(userDetails, Map.of("userId", saved.getUserId().toString()));

        return new AuthResponse(token, saved.getUserId(), saved.getName(), saved.getEmail(),
                saved.getRole().getName().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails, Map.of("userId", user.getUserId().toString()));

        return new AuthResponse(token, user.getUserId(), user.getName(), user.getEmail(), user.getRole().getName().name());
    }
}