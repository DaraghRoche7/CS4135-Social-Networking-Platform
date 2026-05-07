package com.studyhub.userservice.config;

import com.studyhub.userservice.model.Role;
import com.studyhub.userservice.model.RoleName;
import com.studyhub.userservice.model.User;
import com.studyhub.userservice.repository.RoleRepository;
import com.studyhub.userservice.repository.UserRepository;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Profile("!prod")
public class DemoUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedIfMissing(
            "Student",
            "student@studentmail.ul.ie",
            "Password123!",
            RoleName.STUDENT
        );

        seedIfMissing(
            "Peer Student",
            "peer@studentmail.ul.ie",
            "Password123!",
            RoleName.STUDENT
        );

        seedIfMissing(
            "Admin",
            "admin@ul.ie",
            "Password123!",
            RoleName.ADMIN
        );
    }

    private void seedIfMissing(String name, String email, String password, RoleName roleName) {
        if (userRepository.existsByEmail(email)) return;

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalStateException(roleName.name() + " role not found — ensure DataInitializer ran"));

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setRefreshToken(UUID.randomUUID().toString());

        userRepository.save(user);
    }
}

