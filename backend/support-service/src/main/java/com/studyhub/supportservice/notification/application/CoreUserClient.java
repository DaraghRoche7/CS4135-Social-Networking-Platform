package com.studyhub.supportservice.notification.application;

import com.studyhub.supportservice.config.AppProperties;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
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
        int attempts = Math.max(1, appProperties.getCoreService().getMaxAttempts());
        int backoffMs = Math.max(0, appProperties.getCoreService().getBackoffMs());

        for (int attempt = 1; attempt <= attempts; attempt++) {
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

                boolean retryableStatus = ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
                if (!retryableStatus || attempt == attempts) {
                    throw new IllegalStateException(
                        "Core service user validation failed with status %s".formatted(ex.getStatusCode().value()),
                        ex
                    );
                }
            } catch (RestClientException ex) {
                // Covers network failures/timeouts (connect/read).
                if (attempt == attempts) {
                    throw new IllegalStateException("Core service user validation is unavailable", ex);
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Core service user validation is unavailable", ex);
            }

            if (backoffMs > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep((long) backoffMs * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new IllegalStateException("Core service user validation is unavailable");
    }
}
