package org.example.mopl.common.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
    private String host;
    private int port;
    private Duration connectTimeout;
    private Lettuce lettuce;

    @Getter
    @Setter
    public static class Lettuce {
        private Pool pool;

        @Getter
        @Setter
        public static class Pool {
            private int maxActive;
            private int maxIdle;
            private int minIdle;
            private Duration maxWait;
        }
    }
}
