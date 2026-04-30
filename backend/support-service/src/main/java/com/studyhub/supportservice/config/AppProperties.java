package com.studyhub.supportservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final CoreService coreService = new CoreService();
    private final Messaging messaging = new Messaging();
    private final Security security = new Security();

    public Cors getCors() {
        return cors;
    }

    public CoreService getCoreService() {
        return coreService;
    }

    public Messaging getMessaging() {
        return messaging;
    }

    public Security getSecurity() {
        return security;
    }

    public static class Cors {

        @NotEmpty
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class CoreService {

        @NotBlank
        private String baseUrl = "http://localhost:8081";

        @NotBlank
        private String internalApiKey = "change-me-internal-api-key";

        /**
         * Network timeouts for outbound Core Service calls.
         */
        private int connectTimeoutMs = 1500;
        private int readTimeoutMs = 2500;

        /**
         * Small bounded retry for transient Core failures (timeouts/5xx).
         */
        private int maxAttempts = 2;
        private int backoffMs = 200;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getInternalApiKey() {
            return internalApiKey;
        }

        public void setInternalApiKey(String internalApiKey) {
            this.internalApiKey = internalApiKey;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getBackoffMs() {
            return backoffMs;
        }

        public void setBackoffMs(int backoffMs) {
            this.backoffMs = backoffMs;
        }
    }

    public static class Messaging {

        private boolean enabled;

        @NotBlank
        private String exchange = "studyhub.events";

        @NotBlank
        private String loginQueue = "studyhub.support.user-login";

        @NotBlank
        private String loginRoutingKey = "auth.user.login";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getLoginQueue() {
            return loginQueue;
        }

        public void setLoginQueue(String loginQueue) {
            this.loginQueue = loginQueue;
        }

        public String getLoginRoutingKey() {
            return loginRoutingKey;
        }

        public void setLoginRoutingKey(String loginRoutingKey) {
            this.loginRoutingKey = loginRoutingKey;
        }
    }

    public static class Security {

        @NotBlank
        private String jwtSecret = "change-me-local-dev-secret-change-me";

        private boolean allowDemoToken;

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public boolean isAllowDemoToken() {
            return allowDemoToken;
        }

        public void setAllowDemoToken(boolean allowDemoToken) {
            this.allowDemoToken = allowDemoToken;
        }
    }
}
