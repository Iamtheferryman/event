package com.cyril.event.channel;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cyril.event.Event;
import com.cyril.event.exception.ConfirmEventException;
import com.cyril.event.exception.ConsumeEventException;
import com.cyril.event.exception.ProcessingEventException;
import com.cyril.event.exception.PublishEventException;
import com.cyril.event.handle.RetryStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * redis 事件依赖
 */
@Slf4j
public class RedisEventChannel implements EventChannel {

    private RedisTemplate<String, String> redisTemplate;

    private final String eventKey;

    // 处理中事件
    private final String eventProcessingKey;

    public RedisEventChannel(RedisTemplate<String, String> redisTemplate, String topic, String processing) {
        this.redisTemplate = redisTemplate;
        this.eventKey = topic;
        this.eventProcessingKey = processing;
    }

    @Override
    public void publish(Event event) throws PublishEventException {
        publish(event, eventKey);
    }

    @Override
    public <T> void publish(String type, T param){
        publish(type,param,EventOtherParam.builder().retryStrategy(RetryStrategy.ALWAYS).build());
    }

    @Override
    public <T> void publish(String type, T param, EventOtherParam eventOtherParam) {
        Event<T> event = new Event<>(type, param);
        event.setRetryStrategy(eventOtherParam.getRetryStrategy().getType());
        try {
            publish(event);
        } catch (PublishEventException e) {
            log.error(String.format("发送事件异常.事件类型: %s, 事件参数: %s", type, JSON.toJSONString(param)), e);
        }
    }

    @Override
    public <T> void unsafePublish(String type, T param) {
        try {
            publish(type,param,EventOtherParam.builder().retryStrategy(RetryStrategy.ALWAYS).build());

        } catch (Exception e) {
            log.error(String.format("发送事件异常.事件类型: %s, 事件参数: %s", type, JSON.toJSONString(param)), e);
        }
    }

    private void publish(Event event, String key) throws PublishEventException {
        try {
            String eventId = UUID.randomUUID().toString().replaceAll("-", "");
            event.setEventId(eventId);
            redisTemplate.opsForList().leftPush(key, JSONObject.toJSONString(event));
        } catch (Exception e) {
            throw new PublishEventException(e);
        }
    }

    @Override
    public Event consumer() throws ConsumeEventException {
        try {
            String consume = redisTemplate.opsForList().rightPop(eventKey);
            if (StringUtils.isNotEmpty(consume)) {
                return JSON.parseObject(consume, Event.class);
            }
        } catch (Exception e) {
            throw new ConsumeEventException(e);
        }
        return null;
    }

    @Override
    public void processing(String processorName, Event event) throws ProcessingEventException {
        try {
            redisTemplate.opsForHash().put(eventProcessingKey, event.getEventId() + "_" + processorName, JSON
                    .toJSONString(event));
        } catch (Exception e) {
            throw new ProcessingEventException(e);
        }
    }

    @Override
    public void confirm(String processorName, String eventId) throws ConfirmEventException {
        try {
            if (!StringUtils.isEmpty(eventId)) {
                redisTemplate.opsForHash().delete(eventProcessingKey, eventId + "_" + processorName);
            }
        } catch (Exception e) {
            throw new ConfirmEventException(e);
        }
    }


    @Override
    public Map<String, Event> getProcessingEvent() {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> map = hashOps.entries(eventProcessingKey);
        Map<String, Event> resultMap = new HashMap();
        map.forEach((k, v) -> resultMap.put(k, JSON.parseObject(v, Event.class)));
        return resultMap;
    }


    public Map<Object, Object> listOfProcessingEvent() {
        return redisTemplate.opsForHash().entries(eventProcessingKey);
    }

    public Long countOfProcessingEvent() {
        return redisTemplate.opsForHash().size(eventProcessingKey);
    }

    public void deleteProcessingEvent(String hashKey) {
        redisTemplate.opsForHash().delete(eventProcessingKey, hashKey);
    }

}
