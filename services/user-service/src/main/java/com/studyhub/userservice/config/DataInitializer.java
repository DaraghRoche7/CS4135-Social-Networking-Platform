package com.studyhub.userservice.config;

import com.studyhub.userservice.model.Role;
import com.studyhub.userservice.model.RoleName;
import com.studyhub.userservice.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    @Override
    public void run(ApplicationArguments args) {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                log.info("Created role: {}", roleName);
            }
        }
    }
}