package com.cyril.event.worker;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.cyril.event.annoations.BroadCastConsumer;
import com.cyril.event.annoations.BroadcastConsumerInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
public class OnsBroadcastEventWorker implements MessageListener, DisposableBean, InitializingBean  {

    private final Properties properties;
    private final String topic;
    private Consumer consumer;

    private AbstractApplicationContext applicationContext;
    private final Map<String,Object> consumeMap = new ConcurrentHashMap<>();


    public OnsBroadcastEventWorker(AbstractApplicationContext applicationContext,Properties properties, String topic) {
        if (properties == null || properties.get(PropertyKeyConst.ConsumerId) == null
                || properties.get(PropertyKeyConst.AccessKey) == null
                || properties.get(PropertyKeyConst.SecretKey) == null
                || properties.get(PropertyKeyConst.ONSAddr) == null) {
            throw new ONSClientException("consumer properties not set properly.");
        }
        this.properties = properties;
        this.topic = topic;
        this.applicationContext=applicationContext;
    }



    private Stream<String> getBeanNamesByAnnotation(Class<? extends Annotation> annotationType, Class<? extends Annotation> beanType) throws Exception {

        return Stream.of(applicationContext.getBeanNamesForAnnotation(beanType))
                .filter(name -> {
                    final BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
                    final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationType);

                    if (!beansWithAnnotation.isEmpty()) {
                        return beansWithAnnotation.containsKey(name);
                    } else if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                        StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                        return metadata.isAnnotated(annotationType.getName());
                    }

                    return false;
                });
    }

    private <T> Stream<String> getBeanNamesByTypeWithAnnotation(Class<? extends Annotation> annotationType, Class<T> beanType) throws Exception {

        return Stream.of(applicationContext.getBeanNamesForType(beanType))
                .filter(name -> {
                    final BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
                    final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationType);

                    if (!beansWithAnnotation.isEmpty()) {
                        return beansWithAnnotation.containsKey(name);
                    } else if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                        StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                        return metadata.isAnnotated(annotationType.getName());
                    }

                    return false;
                });
    }

    private void initConsumeMap() {
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(BroadCastConsumer.class);
        if(beansWithAnnotation!=null){
            beansWithAnnotation.forEach(
                    (k,v)->{
                        BroadCastConsumer consumerA=AnnotationUtils.findAnnotation(v.getClass(), BroadCastConsumer.class);
                        if(consumerA!=null) {
                            consumeMap.put(consumerA.tag(), v);
                        }
                    }
            );
        }
    }


    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        if (log.isDebugEnabled()) {
            log.info("接收消息:[topic: {}, tag: {}, msgId: {}, startDeliverTime: {}, 重试次数: {}]", message.getTopic(),
                    message.getTag(), message.getMsgID(), message.getStartDeliverTime(),message.getReconsumeTimes());
        }
        try {
            //eg:ProductUpdate
            if( consumeMap.containsKey(message.getTag())){
                BroadcastConsumerInterface broadCastConsumer= (BroadcastConsumerInterface) consumeMap.get(message.getTag());
                broadCastConsumer.consume(JSON.parseObject(new String(message.getBody(),"utf8")));
            }
            return Action.CommitMessage;
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
        initConsumeMap();
        this.consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe(topic, "*", this);
        this.consumer.start();
    }


}
