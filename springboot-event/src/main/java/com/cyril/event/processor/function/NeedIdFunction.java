package com.cyril.event.processor.function;

import java.lang.reflect.Type;

public abstract class NeedIdFunction<T> extends Function<T> {

    public NeedIdFunction(Type type, long timeout) {
        super(type, timeout);
    }

    public abstract boolean execute(String eventId, T param);
}
