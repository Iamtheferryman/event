package com.cyril.event.handle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * ALWAYS，DROP 对ons 队列不生效，如果ons指定为ALWAYS，DROP ，同NONE
 */
public enum RetryStrategy {

    ONLY_ONE(1,"只执行一次"),
    DROP(2, "类斐波纳切数列重试指定次数后丢弃"),
    /**
     * redis 默认
     */
    ALWAYS(3, "类斐波纳切数列重试指定次数后一直按照指定次数重试"),
    /**
     * ons 默认
     */
    NONE(4,"什么也不做");

    RetryStrategy(int name, String desc) {
        this.type = name;
        this.desc = desc;
    }

    private int type;
    private String desc;

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    static Map<Integer, RetryStrategy> allMap = new HashMap<Integer, RetryStrategy>();

    static {
        for (final RetryStrategy strategy : EnumSet.allOf(RetryStrategy.class)) {
            allMap.put(strategy.getType(), strategy);
        }
    }

    public static RetryStrategy getByType(int code) {
        return allMap.get(code);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
