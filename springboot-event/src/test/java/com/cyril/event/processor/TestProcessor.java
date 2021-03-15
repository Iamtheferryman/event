package com.cyril.event.processor;

import com.cyril.event.processor.function.EventType;
import com.cyril.event.processor.function.FunctionProcessor;
import org.springframework.stereotype.Component;

@Component
public class TestProcessor extends FunctionProcessor {

    @EventType(type = "10010")
    public void finishOrder(Object param) {
        System.out.println("get param-------------- " + param.toString());
    }

    @EventType(type = "10011")
    public void finishOrder2(String eventId, Object param) throws InterruptedException {
        System.out.println("get param #################" + param.toString() + ",eventId " + eventId);
        Thread.sleep(1000);
    }
}
