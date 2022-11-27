package com.example.account.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisRepositoryConfig {

    @Value("127.0.0.1") //${spring.redis.host}
    private String redisHost;

    @Value("6379") //@{spring.redis.port}
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {

        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);

        return Redisson.create(config);
    }


}
