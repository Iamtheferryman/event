package com.cyril.event.exception;

/**
 * 处理事件中异常
 */
public class ProcessingEventException extends Exception {
    public ProcessingEventException() {
    }

    public ProcessingEventException(Exception e) {
        super(e);
    }

    public ProcessingEventException(String e) {
        super(e);
    }

    public ProcessingEventException(Throwable e) {
        super(e);
    }
}
