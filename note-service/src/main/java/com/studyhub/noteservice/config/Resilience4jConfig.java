package com.studyhub.noteservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration for the Note Service.
 * <p>
 * Circuit breaker, retry, and time limiter instances are configured in application.yml
 * under the "resilience4j" section. The "noteEventPublisher" instance is applied to
 * event publishing methods in NoteServiceImpl to handle RabbitMQ connectivity issues
 * gracefully.
 * <p>
 * Configuration summary (from application.yml):
 * <ul>
 *   <li>Circuit Breaker: sliding window of 10 calls, 50% failure threshold,
 *       10s wait in open state, 3 calls permitted in half-open state</li>
 *   <li>Retry: maximum 3 attempts with 1s wait between retries</li>
 *   <li>Time Limiter: 3s timeout per call</li>
 * </ul>
 * <p>
 * Annotations used in service layer:
 * - @CircuitBreaker(name = "noteEventPublisher", fallbackMethod = "...")
 * - @Retry(name = "noteEventPublisher")
 */
@Configuration
public class Resilience4jConfig {

    /*
     * All resilience4j configuration is handled declaratively via application.yml.
     * This class serves as a marker and documentation holder for the circuit breaker
     * strategy used in the Note Service.
     *
     * If programmatic customization is needed in the future, CircuitBreakerRegistry
     * and RetryRegistry beans can be defined here.
     */
}
