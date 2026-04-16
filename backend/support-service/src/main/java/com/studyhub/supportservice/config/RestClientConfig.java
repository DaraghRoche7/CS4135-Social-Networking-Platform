package com.studyhub.supportservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient coreServiceRestClient(RestClient.Builder builder, AppProperties appProperties) {
        return builder
            .baseUrl(appProperties.getCoreService().getBaseUrl())
            .build();
    }
}
