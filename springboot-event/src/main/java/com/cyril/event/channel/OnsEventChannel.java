package com.cyril.event.channel;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.cyril.event.Event;
import com.cyril.event.exception.PublishEventException;
import com.cyril.event.handle.RetryStrategy;
import lombok.extern.slf4j.Slf4j;

import javax.swing.border.EmptyBorder;

@Slf4j
public class OnsEventChannel implements EventChannel {

    private ProducerBean producerBean;
    private final String topic;

    public OnsEventChannel(String topic, ProducerBean producerBean) {
        this.topic = topic;
        this.producerBean = producerBean;
    }

    @Override
    public void publish(Event event) throws PublishEventException {
        try {
            Message message = new Message(topic, event.getEventType(), JSON.toJSONBytes(event));
            producerBean.sendOneway(message);
        } catch (Exception e) {
            log.error(String.format("发送事件异常.事件类型: %s, 事件参数: %s", event.getEventType(), JSON.toJSONString(event)), e);
            throw new PublishEventException(e);
        }
    }

    @Override
    public <T> void publish(String type, T param) throws PublishEventException {
        publish(type, param, EventOtherParam.builder().retryStrategy(RetryStrategy.ONLY_ONE).build());
    }

    @Override
    public <T> void publish(String type, T param, EventOtherParam eventOtherParam) throws PublishEventException {
        Event<T> event = new Event<>(type, param);
        event.setRetryStrategy(eventOtherParam.getRetryStrategy().getType());
        publish(event);
    }

    @Override
    public <T> void unsafePublish(String type, T param) {
        try {
            publish(type, param, EventOtherParam.builder().retryStrategy(RetryStrategy.ONLY_ONE).build());
        } catch (Exception e) {
            log.error("send msg error", e);
        }
    }

}
