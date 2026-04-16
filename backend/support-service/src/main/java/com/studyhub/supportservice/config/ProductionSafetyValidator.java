package com.studyhub.supportservice.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ProductionSafetyValidator {

    private static final String DEFAULT_SECRET = "change-me-local-dev-secret-change-me";

    @Bean
    ApplicationRunner validateProductionConfiguration(AppProperties appProperties, Environment environment) {
        return args -> {
            for (String profile : environment.getActiveProfiles()) {
                if ("prod".equalsIgnoreCase(profile)
                    && DEFAULT_SECRET.equals(appProperties.getSecurity().getJwtSecret())) {
                    throw new IllegalStateException(
                        "APP_JWT_SECRET must be overridden when running with the prod profile");
                }
            }
        };
    }
}
