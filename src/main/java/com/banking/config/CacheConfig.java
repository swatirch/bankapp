package com.banking.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // cache entries expire after 10 minutes
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(
                                new GenericJackson2JsonRedisSerializer())); // store values as JSON

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

}
