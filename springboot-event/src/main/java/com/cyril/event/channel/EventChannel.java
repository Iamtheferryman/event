package com.cyril.event.channel;


import com.cyril.event.Event;
import com.cyril.event.exception.ConfirmEventException;
import com.cyril.event.exception.ConsumeEventException;
import com.cyril.event.exception.ProcessingEventException;
import com.cyril.event.exception.PublishEventException;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * EventChannel
 */
public interface EventChannel {
    void publish(Event event) throws PublishEventException;

    /**
     * 未投递成功时，抛出异常
     * @param type
     * @param param
     * @param <T>
     * @throws PublishEventException
     */
    <T> void publish(String type, T param) throws PublishEventException;

    /**
     * 未投递成功时，抛出异常,增加其他参数
     * @param type
     * @param param
     * @param <T>
     * @throws PublishEventException
     */
    <T> void publish(String type, T param, EventOtherParam eventOtherParam) throws PublishEventException;

    /**
     * 未投递成功时，忽略异常
     * @param type
     * @param param
     * @param <T>
     */
    <T> void unsafePublish(String type, T param) ;



    default Event consumer() throws ConsumeEventException {
        return null;
    }

    default void confirm(String processorName, String eventId) throws ConfirmEventException {

    }

    default void processing(String processorName, Event event) throws ProcessingEventException {

    }



    default Map<String, Event> getProcessingEvent() {
        return Maps.newHashMap();
    }
}
