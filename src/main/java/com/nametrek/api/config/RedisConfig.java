package com.nametrek.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    /**
     * Configures the RedisTemplate to use a generic JSON serializer 
     * for both values and hash values.
     *
     * @param connectionFactory the Redis connection factory that 
     *                          provides a connection to the Redis instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer valuesSerializer = new GenericJackson2JsonRedisSerializer();
        StringRedisSerializer keysSerializer = new StringRedisSerializer();

        template.setKeySerializer(keysSerializer);
        template.setHashKeySerializer(keysSerializer);

        template.setValueSerializer(valuesSerializer);
        template.setHashValueSerializer(valuesSerializer);


        return template;
    }
}
