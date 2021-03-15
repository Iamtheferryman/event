package com.cyril.event.handle;

import com.cyril.event.Event;

public class OnsRetryHandler implements RetryHandler {
    @Override
    public boolean shouldJumpCurrRun(Event event) {
        return false;
    }

    @Override
    public boolean shouldExit(Event event) {
        int retryStrategyInt = event.getRetryStrategy();
        RetryStrategy retryStrategy = RetryStrategy.getByType(retryStrategyInt);
        switch (retryStrategy){
            case ONLY_ONE:{
                return event.getTryCount() >=1 ;
            }
            case DROP:
            case ALWAYS:{
                return false;
            }
        }
        return false;
    }
}
