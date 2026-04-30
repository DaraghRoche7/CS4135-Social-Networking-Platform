package com.studyhub.coreservice.module.application;

import com.studyhub.coreservice.auth.application.UserProvisioningService;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.module.domain.FollowedModule;
import com.studyhub.coreservice.module.persistence.FollowedModuleRepository;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModuleService {

    private final FollowedModuleRepository followedModuleRepository;
    private final UserProvisioningService userProvisioningService;
    private final Clock clock;

    public ModuleService(
            FollowedModuleRepository followedModuleRepository,
            UserProvisioningService userProvisioningService,
            Clock clock
    ) {
        this.followedModuleRepository = followedModuleRepository;
        this.userProvisioningService = userProvisioningService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<String> listFollowedModules(String currentUserId) {
        StudyHubUser user = userProvisioningService.getOrProvision(currentUserId);
        return followedModuleRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(FollowedModule::getModuleCode)
                .toList();
    }

    @Transactional
    public List<String> followModule(String currentUserId, String rawModuleCode) {
        StudyHubUser user = userProvisioningService.getOrProvision(currentUserId);
        String normalized = normalizeRequiredModule(rawModuleCode);
        if (!followedModuleRepository.existsByUserAndModuleCodeIgnoreCase(user, normalized)) {
            followedModuleRepository.save(new FollowedModule(user, normalized, clock.instant()));
        }
        return followedModuleRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(FollowedModule::getModuleCode)
                .toList();
    }

    @Transactional
    public List<String> unfollowModule(String currentUserId, String rawModuleCode) {
        StudyHubUser user = userProvisioningService.getOrProvision(currentUserId);
        String normalized = normalizeRequiredModule(rawModuleCode);
        followedModuleRepository.findByUserAndModuleCodeIgnoreCase(user, normalized)
                .ifPresent(followedModuleRepository::delete);
        return followedModuleRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(FollowedModule::getModuleCode)
                .toList();
    }

    public static String normalizeRequiredModule(String moduleCode) {
        if (moduleCode == null) {
            throw new IllegalArgumentException("Module code is required");
        }
        String trimmed = moduleCode.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Module code is required");
        }
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (upper.matches("\\d+")) {
            upper = "CS" + upper;
        }
        if (upper.length() > 40) {
            throw new IllegalArgumentException("Module code is too long");
        }
        return upper;
    }
}