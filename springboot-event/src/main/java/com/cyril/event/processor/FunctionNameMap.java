package com.cyril.event.processor;

import com.cyril.event.processor.function.Function;
import com.google.common.collect.Maps;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 根据类名取得processor的function
 */
@Component
@Primary
public class FunctionNameMap {

    private Map<String, Function> functionMap = Maps.newConcurrentMap();

    public Function getFunction(String name) {
        return functionMap.get(name);
    }

    public void setFunction(String name, Function function) {
        functionMap.put(name, function);
    }

}
