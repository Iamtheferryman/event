package com.cyril.event.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.cyril.event.channel.OnsEventChannel;
import com.cyril.event.handle.OnsRetryHandler;
import com.cyril.event.processor.FunctionMap;
import com.cyril.event.processor.FunctionNameMap;
import com.cyril.event.worker.EventExecutor;
import com.cyril.event.worker.OnsEventWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(OnsMQProperties.class)
@ConditionalOnProperty(prefix = "mq.ons", value = "enabled", havingValue = "true")
@Slf4j
@ComponentScan("com.cyril.event.processor")
public class OnsMQAutoConfiguration {

    @Autowired
    private OnsMQProperties propConfig;

    @Bean(name = "producer", initMethod = "start", destroyMethod = "shutdown")
    @Primary
    public ProducerBean producer() {
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ProducerId, propConfig.getProducer());
        properties.put(PropertyKeyConst.AccessKey, propConfig.getAccessKey());
        properties.put(PropertyKeyConst.SecretKey, propConfig.getSecretKey());
        properties.put(PropertyKeyConst.ONSAddr, propConfig.getOnsAddr());
        producerBean.setProperties(properties);
        producerBean.start();
        return producerBean;
    }

    @Bean
    @Primary
    public OnsEventChannel eventChannel(ProducerBean producerBean) {
        OnsEventChannel eventChannel = new OnsEventChannel(propConfig.getTopic(), producerBean);
        return eventChannel;
    }

    @Autowired
    FunctionMap functionMap;

    @Autowired
    FunctionNameMap functionNameMap;

    @Bean(name = "onsEventExecutor")
    @Primary
    @ConditionalOnProperty(prefix = "mq.ons", value = "consumer_enabled", havingValue = "true")
    public EventExecutor eventExecutor(OnsEventChannel eventChannel) {
        return new EventExecutor(eventChannel, functionNameMap, functionMap);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "mq.ons", value = "consumer_enabled", havingValue = "true")
    public OnsEventWorker consumerWorker(@Qualifier("onsEventExecutor") EventExecutor eventExecutor) {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.ConsumerId, propConfig.getConsumer());
        properties.setProperty(PropertyKeyConst.AccessKey, propConfig.getAccessKey());
        properties.setProperty(PropertyKeyConst.SecretKey, propConfig.getSecretKey());
        properties.setProperty(PropertyKeyConst.ONSAddr, propConfig.getOnsAddr());
        return new OnsEventWorker(properties, eventExecutor, propConfig.getTopic(),new OnsRetryHandler());
    }

}
