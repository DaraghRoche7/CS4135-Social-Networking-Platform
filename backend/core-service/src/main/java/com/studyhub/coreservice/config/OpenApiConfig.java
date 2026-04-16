package com.studyhub.coreservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI coreServiceOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("StudyHub Core Service API")
                .description("Authentication and internal identity endpoints for StudyHub")
                .version("v1"));
    }
}
