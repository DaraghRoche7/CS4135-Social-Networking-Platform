package com.studyhub.coreservice.config;

import java.util.Arrays;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ProductionSafetyValidator {

    @Bean
    ApplicationRunner validateProductionSecrets(Environment environment, AppProperties appProperties) {
        return args -> {
            boolean productionProfileActive = Arrays.stream(environment.getActiveProfiles())
                .anyMatch("prod"::equalsIgnoreCase);

            if (!productionProfileActive) {
                return;
            }

            if ("change-me-local-dev-secret-change-me".equals(appProperties.getSecurity().getJwtSecret())) {
                throw new IllegalStateException("APP_JWT_SECRET must be overridden when the prod profile is active");
            }

            if ("change-me-internal-api-key".equals(appProperties.getInternal().getApiKey())) {
                throw new IllegalStateException("APP_INTERNAL_API_KEY must be overridden when the prod profile is active");
            }
        };
    }
}
