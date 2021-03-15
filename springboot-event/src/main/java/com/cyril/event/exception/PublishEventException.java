package com.cyril.event.exception;

/**
 */
public class PublishEventException extends Exception {
    public PublishEventException(){};
    public PublishEventException(Exception e){super(e);};
    public PublishEventException(String e){super(e);};
    public PublishEventException(Throwable e){super(e);};
}
