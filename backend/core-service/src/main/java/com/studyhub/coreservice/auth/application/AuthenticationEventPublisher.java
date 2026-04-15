package com.studyhub.coreservice.auth.application;

import com.studyhub.coreservice.auth.domain.StudyHubUser;

public interface AuthenticationEventPublisher {

    void publishLoginSuccess(StudyHubUser user);
}
