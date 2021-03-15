package com.cyril.event.processor.function;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件类型
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EventType {

    String type() default "";

    String[] types() default {};


    /**
     * 事件处理的超期时间, 超过这个时间的事件就不处理了
     * 比如timeout设置1小时
     * 这时候收到一个1小时之前发出来的消息,就不会处理它
     * <p>
     * 0 表示事件不过期，任何时候收到都会处理，默认是0
     *
     * @return 超期时间, 单位毫秒
     */
    long timeout() default 0L;
}
