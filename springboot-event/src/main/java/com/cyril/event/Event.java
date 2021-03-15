package com.cyril.event;

/**
 */
public class Event<T> {
    private String eventType;
    private T eventParams;
    String eventId; // 事件id
    private long ts; // 事件发生时间戳
    private int tryCount ; // 重试次数
    private long lastTryTime ;
    private int retryStrategy ; // 重试策略

    public Event() {
        this.ts = System.currentTimeMillis();
    }

    public Event(String eventType, T eventParams) {
        this.eventType = eventType;
        this.eventParams = eventParams;
        this.eventId = EventId.id();
        this.ts = System.currentTimeMillis();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public T getEventParams() {
        return eventParams;
    }

    public void setEventParams(T eventParams) {
        this.eventParams = eventParams;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public int getTryCount() {
        return tryCount;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public long getLastTryTime() {
        return lastTryTime;
    }

    public void setLastTryTime(long lastTryTime) {
        this.lastTryTime = lastTryTime;
    }

    public int getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(int retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    public static <T> Event<T> from(Event<T> event) {
        if (event == null) {
            return event;
        }
        Event<T> e = new Event<>();
        e.setEventId(event.getEventId());
        e.setEventParams(event.getEventParams());
        e.setEventType(event.getEventType());
        e.setTs(event.getTs());
        return e;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
