package com.cyril.event.worker;

import com.alibaba.fastjson.JSON;
import com.cyril.event.Event;
import com.cyril.event.channel.EventChannel;
import com.cyril.event.exception.ConfirmEventException;
import com.cyril.event.exception.ProcessingEventException;
import com.cyril.event.handle.RetryHandler;
import com.cyril.event.processor.FunctionMap;
import com.cyril.event.processor.FunctionNameMap;
import com.cyril.event.processor.function.Function;
import com.cyril.event.processor.function.NeedIdFunction;
import com.cyril.event.processor.function.NoIdFunction;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;

/**
 * event executor
 */
@Slf4j
public class EventExecutor {

    private EventChannel eventChannel;

    public EventChannel getEventChannel() {
        return eventChannel;
    }

    private final FunctionMap functionMap;

    private final FunctionNameMap functionNameMap;

    public EventExecutor(  EventChannel eventChannel,
                         FunctionNameMap functionNameMap, FunctionMap functionMap) {
        this.eventChannel = eventChannel;
        this.functionMap = functionMap;
        this.functionNameMap = functionNameMap;
    }

    private List<Function> getProcessorFunction(String type) {
        return functionMap.getFunction(type);
    }

    public boolean executeSync(Event event){
        boolean success  = true;
        MDC.put("traceId", event.getEventId());
        List<Function> functionList = getProcessorFunction(event.getEventType());
        // 有对应的处理器
        if (functionList != null && functionList.size() > 0) {
            String type = event.getEventType();
            log.info(String.format("function event start, event type: %s ,  event message: %s , received by " +
                    "channel: %s", type, JSON.toJSONString(event.getEventParams()), eventChannel.getClass()
                    .getSimpleName()));
            for (Function function : functionList) {
                boolean once = functionInvoke(function, event);
                success = success && once;
            }
        } else {
            log.warn(String.format("no function to process this event, event type: %s , event message: %s", event
                    .getEventType(), JSON.toJSONString(event.getEventParams())));
        }
        return success;
    }

    /**
     * @param function 方法
     * @param event    事件属性
     * @param <T>      参数泛型
     */
    private <T> boolean functionInvoke(Function<T> function, Event<T> event) {
        try {
            if (!checkEventTimeout(function, event)) {
                confirm(function.getName(), event.getEventId());
                log.info("event time out, event : {}", JSON.toJSONString(event));
                return true;
            }
            Event<T> ev = Event.from(event);
            ev.setEventId(event.getEventId());
            T param = JSON.parseObject(JSON.toJSONString(event.getEventParams()), function.getType());
            ev.setEventParams(param);
            try {
                eventChannel.processing(function.getName(), ev);
            } catch (ProcessingEventException e) {
                log.error(String.format("processing event error, event type: %s , event message: %s", event
                        .getEventType(), JSON.toJSONString(event.getEventParams())), e);
            }
            boolean success = false;
            if (function instanceof NoIdFunction) {
                success = ((NoIdFunction) function).execute(param);
            } else if (function instanceof NeedIdFunction) {
                success = ((NeedIdFunction) function).execute(event.getEventId(), param);
            }

            if (success) {
                confirm(function.getName(), event.getEventId());
            }

            return success;
        } catch (Exception e) {
            log.error(String.format("process event error, event type: %s , event message: %s", event.getEventType(),
                    JSON.toJSONString(event.getEventParams())), e);
            return false;
        }
    }

    private <T> boolean checkEventTimeout(Function<T> function, Event<T> event) {
        if (function.getTimeout() == 0L) {
            // 方法未设置超时时间
            return true;
        } else if (System.currentTimeMillis() - event.getTs() > function.getTimeout()) {
            // 超时了，返回不处理
            return false;
        }
        return true;
    }

    private void confirm(String functionName, String eventId) {
        try {
            eventChannel.confirm(functionName, eventId);
        } catch (ConfirmEventException e) {
            log.error(String.format("confirm event error, event id : %s, processor name : %s", eventId, functionName)
                    , e);
        }
    }


    /**
     * 指定处理器处理event
     *
     * @param functionName 处理器名称
     * @param event        事件
     */
    public void executorFunction(String functionName, Event event) {
        if (event == null) {
            return;
        }
        Function function = getFunctionByName(functionName);
        if (function != null) {
            functionInvoke(function, event);
        } else {
            log.error("function not exist , name : {}, event : {}", functionName, JSON.toJSONString(event));
        }
    }

    private Function getFunctionByName(String functionName) {
        return functionNameMap.getFunction(functionName);
    }


    /**
     *
     * @param event
     * @return 返回值为是否进行重试
     */
    protected final boolean consume(Event event, RetryHandler retryHandler){
        try {
            //如果需要退出的话
            if(retryHandler.shouldExit(event)){
                return false;
            }

            //如果需要跳过此次运行的话，不需要对event里任何属性进行修改
            if(retryHandler.shouldJumpCurrRun(event)){
                return true ;
            }

            boolean success = executeSync(event);
            if(success){
                return false;
            }
            //运行失败的话，增加失败次数以及修改上次失败时间
            addCounter(event);
            return true ;
        }catch (Exception e ){
            addCounter(event);
            log.warn("handle message fail, requeue it.", e);
            return true ;
        }
    }

    /**
     * 对次数和上次运行时间进行修改
     * @param event
     */
    private void addCounter(Event event) {
        event.setTryCount(event.getTryCount() + 1);
        event.setLastTryTime(System.currentTimeMillis());
    }

}
