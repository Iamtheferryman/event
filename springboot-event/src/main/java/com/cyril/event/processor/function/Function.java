package com.cyril.event.processor.function;


import java.lang.reflect.Type;

/**
 * 方法包装对象
 */
public abstract class Function<T> {

    private Type type;

    private String name;

    private long timeout;

    public Function(){

    }

    public Function(Type type, long timeout) {
        this.type = type;
        this.timeout = timeout;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
