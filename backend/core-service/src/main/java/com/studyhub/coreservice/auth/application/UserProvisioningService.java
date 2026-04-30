package com.studyhub.coreservice.auth.application;

import com.studyhub.coreservice.auth.domain.Role;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.persistence.RoleRepository;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(UserProvisioningService.class);

    private final StudyHubUserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserProvisioningService(StudyHubUserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StudyHubUser getOrProvision(String email) {
        return userRepository.findByPublicId(email)
            .or(() -> userRepository.findByEmailIgnoreCase(email))
            .orElseGet(() -> provision(email));
    }

    private StudyHubUser provision(String email) {
        try {
            Role userRole = roleRepository.findByCode("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "Student account access")));
            StudyHubUser user = new StudyHubUser(
                email, email, email, "{noop}EXTERNAL", true, Instant.now(), Set.of(userRole)
            );
            StudyHubUser saved = userRepository.save(user);
            log.info("Auto-provisioned core-service user for {}", email);
            return saved;
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByPublicId(email)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .orElseThrow(() -> new UserNotFoundException(email));
        }
    }
}