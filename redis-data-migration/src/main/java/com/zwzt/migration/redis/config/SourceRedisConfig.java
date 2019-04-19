package com.zwzt.migration.redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Order(100)
//@EnableCaching
public class SourceRedisConfig extends AbstractRedisConfig {

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Bean(value = "redisTemplate_source")
    public RedisTemplate<Object, Object> redisTemplate() {
        return initRedisTemplate();
    }

//    @Bean(value = "initRedisCacheManager_source")
//    public CacheManager initRedisCacheManager() {
//        return super.initRedisCacheManager();
//    }

    @Override
    @Primary
    JedisConnectionFactory getJedisConnectionFactory() {
        System.out.println("source db: " + jedisConnectionFactory.getDatabase());
        return jedisConnectionFactory;
    }
}
