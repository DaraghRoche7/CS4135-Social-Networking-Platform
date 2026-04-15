package ie.ul.studyhub.support.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class CacheConfig {
  @Bean
  CacheManager cacheManager(
      RedisConnectionFactory connectionFactory,
      @Value("${app.cache.feedTtlSeconds:60}") long feedTtlSeconds) {
    RedisCacheConfiguration base =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));

    RedisCacheConfiguration feedConfig = base.entryTtl(Duration.ofSeconds(feedTtlSeconds));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(base)
        .withCacheConfiguration("feed", feedConfig)
        .build();
  }
}

