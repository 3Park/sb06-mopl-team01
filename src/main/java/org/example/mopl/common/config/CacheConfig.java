package org.example.mopl.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager caffeineCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .initialCapacity(1_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .softValues()
                .recordStats()
        );
        return cacheManager;
    }

    @Bean(name = "apiCache")
    public CacheManager apiCaffeineCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .initialCapacity(1_000)
                .refreshAfterWrite(3, TimeUnit.MINUTES)
                .softValues()
                .recordStats()
        );
        return cacheManager;
    }

    @Bean(name = "realTimCache")
    public CacheManager realTimecaffeineCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .refreshAfterWrite(30,TimeUnit.SECONDS)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .recordStats()
        );
        return cacheManager;
    }

    @Bean(name = "longTimeCache")
    public CacheManager longTimecaffeineCacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(15_000)
                .initialCapacity(1_500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .softValues()
                .recordStats()
        );
        return cacheManager;
    }


}
