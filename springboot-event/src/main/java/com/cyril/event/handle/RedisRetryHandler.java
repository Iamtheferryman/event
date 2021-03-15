package com.cyril.event.handle;

import com.alibaba.fastjson.JSONObject;
import com.cyril.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class RedisRetryHandler implements RetryHandler {
    private final static Logger logger = LoggerFactory.getLogger(RedisRetryHandler.class);
    public static Integer[] runTimes = {1, 5, 30, 60, 120, 240, 600, 1800};//, 3600, 7200, 24 * 3600
    public static Integer[] runTimes2 = {100, 500, 600, 800, 1600, 1800};//always, 3600
    public static final int MUST_QUIT_TIMES = 15;


    @Override
    public boolean shouldJumpCurrRun(Event event) {

        return !shouldRun(event);
    }

    public boolean shouldRun(Event event ){
        int tryCount = event.getTryCount();
        RetryStrategy strategy = RetryStrategy.getByType(event.getRetryStrategy());
        long last_execute_time = event.getLastTryTime();
        long now = System.currentTimeMillis();
        switch (strategy) {
            case ONLY_ONE: {
                return tryCount == 0;
            }
            case DROP: {
                if (tryCount == 0) {
                    return true;
                }
                if (tryCount < runTimes.length) {
                    int interval = runTimes[tryCount];
                    if (now > interval * 1000l + last_execute_time) {
                        //should do job
                        return true;
                    } else {
                        //should not do job
                        return false;
                    }
                } else {
                    //should not do job
                    return false;
                }
            }
            case ALWAYS: {
                if (tryCount == 0) {
                    return true;
                }
                if (tryCount < runTimes2.length) {
                    int interval = runTimes2[tryCount];
                    if (now > interval * 1000l + last_execute_time) {
                        //should do job
                        return true;
                    } else {
                        //should not do job
                        return false;
                    }
                } else if (tryCount >= runTimes2.length && tryCount < MUST_QUIT_TIMES) {
                    int interval = runTimes2[runTimes2.length - 1];
                    if (now > interval * 1000l + last_execute_time) {
                        //should do job
                        return true;
                    } else {
                        //should not do job
                        return false;
                    }
                } else {
                    return false;
                }
            }
            default:
                //should not run job
                logger.error("wrong strategy!");
                return false;
        }
    }

    @Override
    public boolean shouldExit(Event event) {
        int tryCount = event.getTryCount();
        RetryStrategy strategy = RetryStrategy.getByType(event.getRetryStrategy());
        switch (strategy) {
            case ONLY_ONE: {
                return tryCount >= 1;
            }
            case DROP: {
                if (tryCount >= runTimes.length) {
                    logger.info("quit job,counter:{},data:{}", tryCount, JSONObject.toJSONString(event));
                    return true;
                } else {
                    return false;
                }
            }
            case ALWAYS: {
                if (tryCount >= MUST_QUIT_TIMES) {
                    logger.info("quit job,counter:{},data:{}", tryCount, JSONObject.toJSONString(event));
                    return true;
                } else {
                    return false;
                }
            }
            default: {
                logger.error("error happens ,unknow strategy!");
                return false;
            }
        }
    }
}
