package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @ClassName MyRedissonConfig
 * @Description
 * @Author wangzuzhen
 * @Create 2020-07-12 22:12
 * @Version 1.0
 */
@Configuration
public class MyRedissonConfig {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private String redisPort;

    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
//        Redis url should start with redis:// or rediss:// (for SSL connection)

        Config config = new Config();
//        config.useClusterServers()
//                .addNodeAddress("redis://127.0.0.1:7004", "redis://127.0.0.1:7001");
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }

}
