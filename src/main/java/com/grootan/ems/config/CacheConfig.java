package com.grootan.ems.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule()); // LocalDate / OffsetDateTime safe

        RedisSerializationContext.SerializationPair<Object> valueSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(om));

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(valueSerializer)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10)); // default TTL

        // Per-cache TTLs (more “production mindset”)
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("departments", defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put("departmentById", defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put("employeeSearch", defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put("employeeById", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        CacheManager manager = RedisCacheManager.builder(cf)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
        log.info("Configured RedisCacheManager with default TTL {} minutes and caches {}", defaultConfig.getTtl().toMinutes(), configs.keySet());
        return manager;
    }
}
