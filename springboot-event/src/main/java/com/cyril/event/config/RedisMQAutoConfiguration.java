package com.cyril.event.config;

import com.cyril.event.channel.RedisEventChannel;
import com.cyril.event.handle.RedisRetryHandler;
import com.cyril.event.processor.FunctionMap;
import com.cyril.event.processor.FunctionNameMap;
import com.cyril.event.worker.EventExecutor;
import com.cyril.event.worker.RedisEventWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties(RedisMQProperties.class)
@EnableCaching
@Slf4j
@ComponentScan("com.cyril.event.processor")
@ConditionalOnProperty(prefix = "mq.redis", value = "enabled", havingValue = "true")
public class RedisMQAutoConfiguration {

    @Autowired
    private RedisMQProperties propConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    public RedisEventChannel redisEventChannel() {
        return new RedisEventChannel(redisTemplate, propConfig.getTopic(), propConfig.getProcessing());
    }

    @Bean(name = "redisThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(30);
        executor.setCorePoolSize(1);
        executor.setThreadNamePrefix("RedisEventExecutor-");
        executor.setQueueCapacity(1000);
        executor.initialize();
        return executor;
    }

    @Autowired
    private FunctionMap functionMap;

    @Autowired
    private FunctionNameMap functionNameMap;

    @Bean(name = "redisEventExecutor")
    public EventExecutor eventExecutor(RedisEventChannel redisEventChannel) {
        return new EventExecutor(redisEventChannel, functionNameMap, functionMap);
    }

    @Bean
    public RedisEventWorker redisEventWorker(@Qualifier("redisEventExecutor") EventExecutor eventExecutor) {

        return new RedisEventWorker(eventExecutor, eventExecutor.getEventChannel(),new RedisRetryHandler());
    }

}
