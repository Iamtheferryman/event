package com.cyril.event.processor.function;

import java.lang.reflect.Type;

public abstract class NoIdFunction<T> extends Function<T> {

    public NoIdFunction(Type type, long timeout) {
        super(type, timeout);
    }

    public abstract boolean execute(T param);
}
