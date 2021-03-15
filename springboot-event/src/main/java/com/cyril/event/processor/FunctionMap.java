package com.cyril.event.processor;

import com.cyril.event.processor.function.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 方法map
 */
@Component
@Primary
public class FunctionMap {

    private Map<String, List<Function>> functionMap = Maps.newHashMap();

    private ReentrantLock lock = new ReentrantLock();

    public List<Function> getFunction(String type) {
        return functionMap.get(type);
    }


    public void setFunction(String type, Function function) {
        lock.lock();
        try {
            List<Function> list = getFunction(type);
            if (list == null) {
                list = Lists.newArrayList();
                functionMap.put(type, list);
            }
            list.add(function);
        } finally {
            lock.unlock();
        }
    }


}
