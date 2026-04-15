package com.studyhub.coreservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final Security security = new Security();
    private final Internal internal = new Internal();
    private final Messaging messaging = new Messaging();
    private final Storage storage = new Storage();

    public Cors getCors() {
        return cors;
    }

    public Security getSecurity() {
        return security;
    }

    public Internal getInternal() {
        return internal;
    }

    public Messaging getMessaging() {
        return messaging;
    }

    public Storage getStorage() {
        return storage;
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

    public static class Security {

        @NotBlank
        private String jwtSecret = "change-me-local-dev-secret-change-me";

        @Positive
        private long tokenLifetimeSeconds = 3600;

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long getTokenLifetimeSeconds() {
            return tokenLifetimeSeconds;
        }

        public void setTokenLifetimeSeconds(long tokenLifetimeSeconds) {
            this.tokenLifetimeSeconds = tokenLifetimeSeconds;
        }
    }

    public static class Internal {

        @NotBlank
        private String apiKey = "change-me-internal-api-key";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Messaging {

        private boolean enabled;

        @NotBlank
        private String exchange = "studyhub.events";

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

        public String getLoginRoutingKey() {
            return loginRoutingKey;
        }

        public void setLoginRoutingKey(String loginRoutingKey) {
            this.loginRoutingKey = loginRoutingKey;
        }
    }

    public static class Storage {

        @NotBlank
        private String postsDir = "data/posts";

        public String getPostsDir() {
            return postsDir;
        }

        public void setPostsDir(String postsDir) {
            this.postsDir = postsDir;
        }
    }
}
