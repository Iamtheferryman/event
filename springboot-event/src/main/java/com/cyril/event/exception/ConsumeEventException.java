package com.cyril.event.exception;

/**
 */
public class ConsumeEventException extends Exception {
    public ConsumeEventException(){}
    public ConsumeEventException(Exception e){ super(e);}
    public ConsumeEventException(String e){ super(e);}
    public ConsumeEventException(Throwable e){ super(e);}
}
