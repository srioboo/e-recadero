package org.sirantar.recadero.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ClientOptions;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Persistence infrastructure configuration for Redis connectivity and serialization.
 *
 * Provides:
 * - Pooled Lettuce connection factory using spring.redis properties
 * - RedisTemplate for JSON value serialization
 * - String key serialization for human-readable keys
 */
@Configuration
public class PersistenceConfig {

  private final RedisProperties redisProperties;
  private final ObjectMapper objectMapper;

  public PersistenceConfig(RedisProperties redisProperties, ObjectMapper objectMapper) {
    this.redisProperties = redisProperties;
    this.objectMapper = objectMapper;
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
    standaloneConfiguration.setHostName(redisProperties.getHost());
    standaloneConfiguration.setPort(redisProperties.getPort());
    standaloneConfiguration.setDatabase(redisProperties.getDatabase());

    if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
      standaloneConfiguration.setPassword(redisProperties.getPassword());
    }

    LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
        .commandTimeout(resolveTimeout(redisProperties.getTimeout()))
        .clientOptions(ClientOptions.builder().autoReconnect(true).build())
        .build();

    return new LettuceConnectionFactory(standaloneConfiguration, clientConfiguration);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);

    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);
    template.setValueSerializer(jsonSerializer);
    template.setHashValueSerializer(jsonSerializer);

    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public StringRedisSerializer stringRedisSerializer() {
    return new StringRedisSerializer();
  }

  @Bean
  public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
    return new GenericJackson2JsonRedisSerializer(objectMapper);
  }

  private Duration resolveTimeout(Duration timeout) {
    Duration resolvedTimeout = timeout != null ? timeout : Duration.ofSeconds(2);
    return resolvedTimeout.isNegative() ? Duration.ofSeconds(2) : resolvedTimeout;
  }
}
