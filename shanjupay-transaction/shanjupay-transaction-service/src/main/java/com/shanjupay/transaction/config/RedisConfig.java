package com.shanjupay.transaction.config;

import com.shanjupay.common.cache.Cache;
import com.shanjupay.transaction.common.util.RedisCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author old money
 * @create 2022-03-15 14:40
 */
@Configuration
public class RedisConfig {


    @Bean
    public Cache cache(StringRedisTemplate stringRedisTemplate){
        return new RedisCache(stringRedisTemplate);
    }

}
