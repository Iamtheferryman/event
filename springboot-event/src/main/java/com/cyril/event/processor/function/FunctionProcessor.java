package com.cyril.event.processor.function;

import com.alibaba.fastjson.JSON;
import com.cyril.event.processor.FunctionMap;
import com.cyril.event.processor.FunctionNameMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

/**
 * function processor
 */
@Slf4j
public abstract class FunctionProcessor {

    @Autowired
    private FunctionMap functionMap;

    @Autowired
    private FunctionNameMap functionNameMap;

    @PostConstruct
    public <T> void initProcessor() {
        Method[] methods = this.getClass().getDeclaredMethods();
        FunctionProcessor processor = this;
        for (Method method : methods) {
            if (method.isAnnotationPresent(EventType.class)) {
                EventType eventType = method.getAnnotation(EventType.class);
                Class[] types = method.getParameterTypes();
                Function<T> function = null;
                if (types != null && types.length == 1) {
                    function = new NoIdFunction<T>(types[0], eventType.timeout()) {
                        @Override
                        public boolean execute(T param) {
                            try {
                                method.invoke(processor, param);
                                return true;
                            } catch (Exception e) {
                                log.error("", e);
                                log.error("processor invoke error {} {} {} {}",
                                        JSON.toJSONString(param), processor.getClass().getCanonicalName(), method
                                                .getName(), e);
                            }
                            return false;
                        }
                    };
                } else if (types != null && types.length == 2) {
                    function = new NeedIdFunction<T>(types[0], eventType.timeout()) {
                        @Override
                        public boolean execute(String eventId, T param) {
                            try {
                                method.invoke(processor, eventId, param);
                                return true;
                            } catch (Exception e) {
                                log.error("", e);
                                log.error("processor invoke error {} {} {} {}",
                                        JSON.toJSONString(param), processor.getClass().getCanonicalName(), method
                                                .getName(), e);
                            }
                            return false;
                        }
                    };

                }

                if (function != null) {
                    String name = this.getClass().getCanonicalName() + "." + method.getName();
                    function.setName(name);
                    functionNameMap.setFunction(name, function);

                    String[] eventTypes = eventType.types();
                    if (eventTypes.length > 0) {
                        // 多个事件
                        for (String type : eventTypes) {
                            functionMap.setFunction(type, function);
                        }
                    } else {
                        // 单个事件
                        functionMap.setFunction(eventType.type(), function);
                    }
                }


            }
        }
    }

}
