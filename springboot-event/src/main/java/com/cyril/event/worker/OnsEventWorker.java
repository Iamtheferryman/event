package com.cyril.event.worker;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.cyril.event.Event;
import com.cyril.event.handle.RetryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Properties;

@Slf4j
public class OnsEventWorker implements MessageListener, DisposableBean, InitializingBean  {

    private final Properties properties;
    private final String topic;
    private Consumer consumer;
    private EventExecutor eventExecutor ;
    private RetryHandler retryHandler ;

    public OnsEventWorker(Properties properties, EventExecutor eventExecutor, String topic, RetryHandler retryHandler) {
        if (properties == null || properties.get(PropertyKeyConst.ConsumerId) == null
                || properties.get(PropertyKeyConst.AccessKey) == null
                || properties.get(PropertyKeyConst.SecretKey) == null
                || properties.get(PropertyKeyConst.ONSAddr) == null) {
            throw new ONSClientException("consumer properties not set properly.");
        }
        this.properties = properties;
        this.eventExecutor = eventExecutor;
        this.topic = topic;
        this.retryHandler = retryHandler;
    }


    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        if (log.isDebugEnabled()) {
            log.info("接收消息:[topic: {}, tag: {}, msgId: {}, startDeliverTime: {}, 重试次数: {}]", message.getTopic(),
                    message.getTag(), message.getMsgID(), message.getStartDeliverTime(),message.getReconsumeTimes());
        }
        try {
            Event event = JSON.parseObject(message.getBody(), Event.class);
            event.setTryCount(message.getReconsumeTimes());
            boolean needTry = eventExecutor.consume(event,retryHandler);
//            boolean success = eventExecutor.executeSync(event);
            return needTry ? Action.ReconsumeLater : Action.CommitMessage;
        } catch (Exception e) {
            //消费失败
            log.warn("handle message fail, requeue it.", e);
            return Action.ReconsumeLater;
        }
    }

    @Override
    public void destroy() {
        if (this.consumer != null) {
            this.consumer.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() {
        this.consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe(topic, "*", this);
        this.consumer.start();
    }
}
