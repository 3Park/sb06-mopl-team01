package org.example.mopl.common.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "eventTaskExecutor")
    public TaskExecutor eventExecutor() {
        ThreadPoolTaskExecutor executor =new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-task-");
        executor.setTaskDecorator(
                new CompositeTaskDecorator(List.of(mdcTaskDecorator(),securityContext()))
        );
        executor.initialize();
        
        return executor;
    }

    @Bean(name = "batchTaskExecutor")
    public TaskExecutor batchExecutor() {
        ThreadPoolTaskExecutor executor =new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("batch-task-");
        executor.setTaskDecorator(
                new CompositeTaskDecorator(List.of(mdcTaskDecorator(),securityContext()))
        );
        executor.initialize();

        return executor;
    }

    /*TMDB RATE LIMIT = 40 requests/second
    이를 고려하여 스레드풀 크기 조정*/
    @Bean(name = "tdDbCrawlTaskExecutor")
    public TaskExecutor tmDbCrawlExecutor() {
        ThreadPoolTaskExecutor executor =new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6); // API 호출 간격 300ms 고려
        executor.setMaxPoolSize(10); // 초당 처리량 : 10 / 300ms = 33.3 requests/second
        executor.setQueueCapacity(30); // TMDb API rate limit 고려
        executor.setThreadNamePrefix("tmdb-crawl-task-");
        executor.setTaskDecorator(
                new CompositeTaskDecorator(List.of(mdcTaskDecorator(),securityContext()))
        );
        executor.initialize();

        return executor;
    }
    
    public TaskDecorator mdcTaskDecorator(){
        return runnable -> {
            Optional<String> requestId = Optional.ofNullable(MDC.get(MDCLoggingInterceptor.REQUEST_ID))
                    .map(String.class::cast);
            return () -> {
                requestId.ifPresent(id -> MDC.put(MDCLoggingInterceptor.REQUEST_ID, id));
                try{
                    runnable.run();
                }finally {
                    requestId.ifPresent(id -> MDC.remove(MDCLoggingInterceptor.REQUEST_ID));
                }
            };
        };
    }
    
    public TaskDecorator securityContext(){
        return runnable -> {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            return () -> {
                SecurityContextHolder.setContext(securityContext);
                try {
                    runnable.run();
                }finally {
                    SecurityContextHolder.clearContext();
                }
            };
        };
    }
}
