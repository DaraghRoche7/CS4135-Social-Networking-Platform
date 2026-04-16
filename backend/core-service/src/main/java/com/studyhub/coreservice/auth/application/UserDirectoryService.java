package com.studyhub.coreservice.auth.application;

import com.studyhub.coreservice.auth.api.dto.UserSummaryResponse;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDirectoryService {

    private final StudyHubUserRepository studyHubUserRepository;

    public UserDirectoryService(StudyHubUserRepository studyHubUserRepository) {
        this.studyHubUserRepository = studyHubUserRepository;
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getUserById(String userId) {
        StudyHubUser user = studyHubUserRepository.findByPublicId(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        return UserSummaryResponse.from(user);
    }
}
