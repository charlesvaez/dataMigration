package com.zwzt.migration.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Configuration
@Order(1)
public class InitRedisConfig {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private Integer redisPort;

    @Value("${redis.db0}")
    private Integer database_source;

    @Value("${redis.db1}")
    private Integer database_desc;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.maxIdle}")
    private Integer maxIdle;

    @Value("${redis.maxTotal}")
    private Integer maxTotal;

    @Value("${redis.connect.timeout}")
    private Integer connectTimeout;

    @Value("${redis.read.timeout}")
    private Integer readTimeout;

    @Value("${redis.timeout}")
    private Integer timeout;

    @Value("${redis.maxWaitMillis}")
    private Integer maxWaitMillis;

    @Value("${redis.testOnCreate}")
    private boolean testOnCreate;

    @Value("${redis.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${redis.testWhileIdle}")
    private boolean testWhileIdle;

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接数
        jedisPoolConfig.setMaxTotal(maxTotal);
        //最小空闲连接数
        jedisPoolConfig.setMinIdle(maxIdle);
        //当池内没有可用的连接时，最大等待时间
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setTestOnCreate(testOnCreate);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestWhileIdle(testWhileIdle);
        return jedisPoolConfig;
    }

    @Bean
    public JedisClientConfiguration jedisClientConfiguration(JedisPoolConfig jedisPoolConfig) {

        //return JedisClientConfiguration.builder().connectTimeout(Duration.ofMillis(connectTimeout)).readTimeout(Duration.ofMillis(readTimeout)).build();

        JedisClientConfiguration.DefaultJedisClientConfigurationBuilder djcb = (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration.builder();
        djcb.readTimeout(Duration.ofMillis(readTimeout));
        djcb.connectTimeout(Duration.ofMillis(connectTimeout));
        djcb.usePooling();
        djcb.poolConfig(jedisPoolConfig);

        //通过构造器来构造jedis客户端配置
        JedisClientConfiguration jedisClientConfiguration = djcb.build();
        return jedisClientConfiguration;
    }

    private RedisConfiguration init(int db) {
        RedisStandaloneConfiguration redisConfiguration =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        redisConfiguration.setDatabase(db);
        if (null != password && !"".equals(password))
            redisConfiguration.setPassword(password);
        return redisConfiguration;
    }

    private RedisConfiguration redisConfiguration_source() {
        return init(database_source);
    }

    private RedisConfiguration redisConfiguration_desc() {
        return init(database_desc);
    }

    /**
     * Jedis 连接工厂.
     *
     * @return 配置好的Jedis连接工厂
     */
    @Bean("jedisConnectionFactory_source")
    @Primary
    public JedisConnectionFactory jedisConnectionFactory_source(JedisClientConfiguration jedisClientConfiguration) {
        RedisConfiguration redisConfiguration_source = redisConfiguration_source();
        JedisConnectionFactory jedisConnectionFactory = null;
        if (redisConfiguration_source instanceof RedisStandaloneConfiguration) {
            jedisConnectionFactory = new JedisConnectionFactory((RedisStandaloneConfiguration) redisConfiguration_source, jedisClientConfiguration);
        } else if (redisConfiguration_source instanceof RedisSentinelConfiguration) {
            jedisConnectionFactory = new JedisConnectionFactory((RedisSentinelConfiguration) redisConfiguration_source, jedisClientConfiguration);
        } else if (redisConfiguration_source instanceof RedisClusterConfiguration) {
            jedisConnectionFactory = new JedisConnectionFactory((RedisClusterConfiguration) redisConfiguration_source, jedisClientConfiguration);
        } else {
            jedisConnectionFactory = new JedisConnectionFactory((RedisStandaloneConfiguration) redisConfiguration_source, jedisClientConfiguration);
        }
        return jedisConnectionFactory;
    }

    /**
     * Jedis 连接工厂.
     *
     * @return 配置好的Jedis连接工厂
     */
    @Bean("jedisConnectionFactory_desc")
    public JedisConnectionFactory jedisConnectionFactory_desc(JedisClientConfiguration jedisClientConfiguration) {
        RedisConfiguration redisConfiguration_desc = this.redisConfiguration_desc();

        if (redisConfiguration_desc instanceof RedisStandaloneConfiguration) {
            return new JedisConnectionFactory((RedisStandaloneConfiguration) redisConfiguration_desc, jedisClientConfiguration);
        }
        if (redisConfiguration_desc instanceof RedisSentinelConfiguration) {
            return new JedisConnectionFactory((RedisSentinelConfiguration) redisConfiguration_desc, jedisClientConfiguration);
        }
        if (redisConfiguration_desc instanceof RedisClusterConfiguration) {
            return new JedisConnectionFactory((RedisClusterConfiguration) redisConfiguration_desc, jedisClientConfiguration);
        }
        return new JedisConnectionFactory((RedisStandaloneConfiguration) redisConfiguration_desc, jedisClientConfiguration);
    }


}
