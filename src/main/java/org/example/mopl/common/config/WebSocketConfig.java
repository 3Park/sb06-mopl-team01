package org.example.mopl.common.config;

import lombok.RequiredArgsConstructor;
import org.example.mopl.common.interceptor.WebSocketChannelInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final WebSocketChannelInterceptor channelInterceptor;


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(websocketTaskScheduler());

        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor)
                .taskExecutor(inboundChannelExecutor());
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor(outboundChannelExecutor());
    }

    @Bean("websocketTaskScheduler")
    public TaskScheduler websocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        scheduler.setThreadNamePrefix("websocket-broker-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.initialize();
        return scheduler;
    }

    @Bean("websocketInboundExecutor")
    public ThreadPoolTaskExecutor inboundChannelExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("inbound-");
        executor.initialize();
        return executor;
    }

    @Bean("websocketOutboundExecutor")
    public ThreadPoolTaskExecutor outboundChannelExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("outbound-");
        executor.initialize();
        return executor;
    }
}
