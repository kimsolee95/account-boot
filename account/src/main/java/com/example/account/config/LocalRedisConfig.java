package com.example.account.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class LocalRedisConfig {

    @Value("6379") //"${spring.redis.port}"
    private int redisPort;
    //private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        redisServer = new RedisServer(redisPort);
        redisServer.start();

//        redisServer = RedisServer.builder()
//                .port(redisPort)
//                .setting("maxmemory 128M")
//                .build();
//        redisServer.start();


//        try {
//            redisServer.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }



}
