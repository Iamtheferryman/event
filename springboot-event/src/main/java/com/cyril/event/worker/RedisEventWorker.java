package com.cyril.event.worker;

import com.cyril.event.Event;
import com.cyril.event.channel.EventChannel;
import com.cyril.event.exception.PublishEventException;
import com.cyril.event.handle.RetryHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

@Slf4j
public class RedisEventWorker implements CommandLineRunner, Runnable, DisposableBean {
    private EventChannel eventChannel;

    private EventExecutor eventExecutor;

    private RetryHandler retryHandler;
    private volatile boolean stop = false;

    @Autowired
    @Qualifier("redisThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public RedisEventWorker(EventExecutor eventExecutor, EventChannel eventChannel, RetryHandler retryHandler) {
        this.eventChannel = eventChannel;
        this.eventExecutor = eventExecutor;
        this.retryHandler = retryHandler;
    }

    @Override
    public void run() {
        checkUnprocessedEvent();
        process();
    }

    private void checkUnprocessedEvent() {
        Map<String, Event> processingEvents = eventChannel.getProcessingEvent();
        for (Map.Entry<String, Event> event : processingEvents.entrySet()) {
            String key = event.getKey();
            if (!StringUtils.isEmpty(key)) {
                String[] name = key.split("_");
                Event value = event.getValue();
                if (name.length == 2) {
                    eventExecutor.executorFunction(name[1], value);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("sleep error", e);
            }
        }
    }

    protected void process() {
        while (!stop) {
            try {
                Event event = eventChannel.consumer();
                if (event == null) {
                    // 没有对象休息100 毫秒
                    Thread.sleep(100);
                } else {
                    threadPoolTaskExecutor.execute(() -> {
                        boolean needRetry = eventExecutor.consume(event,retryHandler);
                        //如果需要重试，重新投递
                        if(needRetry){
                            try {
                                eventChannel.publish(event);
                            } catch (PublishEventException e) {
                                log.error("投递失败" ,e );
                            }
                        }
                    });
                }
            } catch (Exception e) {
                log.error("consume event error" + e.getMessage());
            }
        }
    }

    @Override
    public void run(String... strings) throws Exception {
        startDaemonAwaitThread();
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(this);
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    @Override
    public void destroy() throws Exception {
        stop = true;
    }
}
