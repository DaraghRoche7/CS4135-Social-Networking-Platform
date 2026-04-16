package com.studyhub.coreservice.auth.config;

import com.studyhub.coreservice.auth.domain.Role;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.persistence.RoleRepository;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import java.time.Instant;
import java.util.Set;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!prod")
public class AuthSeedData {

    @Bean
    ApplicationRunner seedUsers(
        RoleRepository roleRepository,
        StudyHubUserRepository studyHubUserRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role userRole = roleRepository.findByCode("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "Student account access")));
            Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Administrative platform access")));

            if (studyHubUserRepository.findByPublicId("demo-user").isEmpty()) {
                studyHubUserRepository.save(new StudyHubUser(
                    "demo-user",
                    "student@studyhub.local",
                    "Demo Student",
                    passwordEncoder.encode("Password123!"),
                    true,
                    Instant.parse("2026-04-15T10:00:00Z"),
                    Set.of(userRole)
                ));
            }

            if (studyHubUserRepository.findByPublicId("admin-user").isEmpty()) {
                studyHubUserRepository.save(new StudyHubUser(
                    "admin-user",
                    "admin@studyhub.local",
                    "Demo Admin",
                    passwordEncoder.encode("Password123!"),
                    true,
                    Instant.parse("2026-04-15T10:05:00Z"),
                    Set.of(userRole, adminRole)
                ));
            }

            if (studyHubUserRepository.findByPublicId("peer-user").isEmpty()) {
                studyHubUserRepository.save(new StudyHubUser(
                    "peer-user",
                    "peer@studyhub.local",
                    "Peer Student",
                    passwordEncoder.encode("Password123!"),
                    true,
                    Instant.parse("2026-04-15T10:10:00Z"),
                    Set.of(userRole)
                ));
            }
        };
    }
}
