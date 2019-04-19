package com.zwzt.migration.redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
//@PropertySource({ "classpath:database-configs.properties" })
@Order(100)
//@EnableCaching
public class DescRedisConfig extends AbstractRedisConfig {

    @Autowired
    @Qualifier("jedisConnectionFactory_desc")
    private JedisConnectionFactory jedisConnectionFactory;

    @Bean(value = "redisTemplate_desc")
    public RedisTemplate<Object, Object> redisTemplate() {
        return initRedisTemplate();
    }

//    @Bean(value = "initRedisCacheManager_desc")
//    public CacheManager initRedisCacheManager() {
//        return super.initRedisCacheManager();
//    }

    @Override
    JedisConnectionFactory getJedisConnectionFactory() {
        System.out.println("desc db: " + jedisConnectionFactory.getDatabase());
        return jedisConnectionFactory;
    }
}
