package com.studyhub.supportservice.notification.application;

import com.studyhub.supportservice.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CoreUserClient {

    private final RestClient restClient;
    private final AppProperties appProperties;

    public CoreUserClient(RestClient coreServiceRestClient, AppProperties appProperties) {
        this.restClient = coreServiceRestClient;
        this.appProperties = appProperties;
    }

    public CoreUserSummaryResponse getUserById(String userId) {
        try {
            CoreUserSummaryResponse response = restClient.get()
                .uri("/internal/users/{userId}", userId)
                .header("X-Internal-Api-Key", appProperties.getCoreService().getInternalApiKey())
                .retrieve()
                .body(CoreUserSummaryResponse.class);

            if (response == null) {
                throw new IllegalStateException("Core service returned an empty user response");
            }

            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new RecipientNotFoundException(userId);
            }
            throw new IllegalStateException("Core service user validation failed with status %s".formatted(ex.getStatusCode().value()), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Core service user validation is unavailable", ex);
        }
    }
}
