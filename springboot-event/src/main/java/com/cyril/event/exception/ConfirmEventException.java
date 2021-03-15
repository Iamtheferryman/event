package com.cyril.event.exception;

/**
 * 确认事件被消费事件
 */
public class ConfirmEventException extends Exception {
    public ConfirmEventException() {
    }

    public ConfirmEventException(Exception e) {
        super(e);
    }

    public ConfirmEventException(String e) {
        super(e);
    }

    public ConfirmEventException(Throwable e) {
        super(e);
    }
}
