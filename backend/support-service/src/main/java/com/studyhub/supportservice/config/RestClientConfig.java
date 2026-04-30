package com.studyhub.supportservice.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient coreServiceRestClient(RestClient.Builder builder, AppProperties appProperties) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(appProperties.getCoreService().getConnectTimeoutMs()))
                .build()
        );
        requestFactory.setReadTimeout(Duration.ofMillis(appProperties.getCoreService().getReadTimeoutMs()));

        return builder
            .baseUrl(appProperties.getCoreService().getBaseUrl())
            .requestFactory(requestFactory)
            .build();
    }
}
