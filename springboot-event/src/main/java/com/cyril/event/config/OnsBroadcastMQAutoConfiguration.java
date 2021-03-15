package com.cyril.event.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.cyril.event.channel.OnsEventChannel;
import com.cyril.event.handle.OnsRetryHandler;
import com.cyril.event.processor.FunctionMap;
import com.cyril.event.processor.FunctionNameMap;
import com.cyril.event.worker.EventExecutor;
import com.cyril.event.worker.OnsBroadcastEventWorker;
import com.cyril.event.worker.OnsEventWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(OnsMQProperties.class)
@ConditionalOnProperty(prefix = "mq.ons.broadcast", value = "enabled", havingValue = "true")
@Slf4j
@ComponentScan("com.cyril.event.processor")
public class OnsBroadcastMQAutoConfiguration {

    @Autowired
    private OnsBroadcastMqProperties onsBroadcastMQProperties;
    @Autowired
    private AbstractApplicationContext applicationContext;

    @Bean(name = "broadcastProducer", initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean broadcastProducer() {
        ProducerBean producerBean = new ProducerBean();
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ProducerId, onsBroadcastMQProperties.getProducer());
        properties.put(PropertyKeyConst.AccessKey, onsBroadcastMQProperties.getAccessKey());
        properties.put(PropertyKeyConst.SecretKey, onsBroadcastMQProperties.getSecretKey());
        properties.put(PropertyKeyConst.ONSAddr, onsBroadcastMQProperties.getOnsAddr());
        producerBean.setProperties(properties);
        producerBean.start();
        return producerBean;
    }

    @Bean(name = "broadcastEventChannel")
    public OnsEventChannel eventChannel(@Qualifier("broadcastProducer") ProducerBean broadcastProducerBean) {
        OnsEventChannel eventChannel = new OnsEventChannel(onsBroadcastMQProperties.getTopic(), broadcastProducerBean);
        return eventChannel;
    }


    @Bean("broadcastConsumerWorker")
    @ConditionalOnProperty(prefix = "mq.ons.broadcast", value = "consumer_enabled", havingValue = "true")
    public OnsBroadcastEventWorker consumerWorker() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.ConsumerId, onsBroadcastMQProperties.getConsumer());
        properties.setProperty(PropertyKeyConst.AccessKey, onsBroadcastMQProperties.getAccessKey());
        properties.setProperty(PropertyKeyConst.SecretKey, onsBroadcastMQProperties.getSecretKey());
        properties.setProperty(PropertyKeyConst.ONSAddr, onsBroadcastMQProperties.getOnsAddr());
        return new OnsBroadcastEventWorker(applicationContext,properties, onsBroadcastMQProperties.getTopic());
    }

}
