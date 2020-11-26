package com.tjj.gulimall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/15
 * Description:
 */
/*@EnableConfigurationProperties(CacheProperties.class)*/
@Configuration
@EnableCaching
public class CacheConfig {

    @Autowired
    CacheProperties cacheProperties;

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){

        /**
         * 配置文件中的东西不生效
         *
         * 原来和配置文件绑定的配置类是这样的
         *  @ConfigurationProperties(
         *      prefix = "spring.cache"
         *  )
         *  public class CacheProperties
         *
         *  导入Bean让其生效
         *    1、@EnableConfigurationProperties(CacheProperties.class)
         *
         *    2、 @Autowired
         *        CacheProperties cacheProperties;
         *
         */

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));//修改key的序列化
        //GenericFastJsonRedisSerializer兼容所有类型
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));//修改value的序列化

        /**
         * 将其元配置中的所有参数生效
         */
        CacheProperties.Redis redis = cacheProperties.getRedis();
        if (redis.getTimeToLive() != null) {
            config = config.entryTtl(redis.getTimeToLive());
        }

        if (redis.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redis.getKeyPrefix());
        }

        if (!redis.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        if (!redis.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }

        return config;
    }
}